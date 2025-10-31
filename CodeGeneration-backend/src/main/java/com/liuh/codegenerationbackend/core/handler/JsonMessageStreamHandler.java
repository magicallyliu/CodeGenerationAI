package com.liuh.codegenerationbackend.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.liuh.codegenerationbackend.ai.model.message.*;
import com.liuh.codegenerationbackend.ai.tools.BaseTool;
import com.liuh.codegenerationbackend.ai.tools.ToolManager;
import com.liuh.codegenerationbackend.constant.AppConstant;
import com.liuh.codegenerationbackend.core.builder.VueProjectBuilder;
import com.liuh.codegenerationbackend.model.entity.User;
import com.liuh.codegenerationbackend.model.enums.ChatHistoryMessageTypeEnum;
import com.liuh.codegenerationbackend.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;

/**
 * @Description JSON 消息流处理器
 * 处理复杂的vue工程的复杂的流式响应, 包含工具的调用
 */

@SuppressWarnings("all")
@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ToolManager toolManager;

    /**
     * 处理JSON消息流
     * 解析 JSON 消息并重组为完整的响应格式, 同时保存到数据库
     *
     * @param originFlux         原始文本流
     * @param chatHistoryService 对话历史服务
     * @param appId              应用id
     * @param loginUser          登录用户
     * @return
     */
    public Flux<String> handle(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               Long appId, User loginUser) {
        //用于收集
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        //用于跟踪已经出现过的工具ID, 以判断是否为第一次调用
        HashSet<String> seenTools = new HashSet<>();

        return originFlux.map(chunk -> {


                    //解析每一个JSON消息快
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenTools);
                })
                .filter(StrUtil::isNotEmpty)//过滤掉空字符串
                .doOnComplete(() -> {
                    //流式返回结束后, 保存对话记忆
                    chatHistoryService.addChatMessage(appId, chatHistoryStringBuilder.toString(),
                            ChatHistoryMessageTypeEnum.AI.getValue(), loginUser);

                    //当所有的流式返回后, 执行构建流程
                    //TODO 暂时使用异步构造
                    String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                    vueProjectBuilder.buildProjectAsync(projectPath);
                }).doOnError(error -> {
                    //即使流式返回失败, 也需要将消息记录到数据库中
                    String errorMessage = "AI 回复消息失败" + error.getMessage();
                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser);
                });
    }


    /**
     * 解析并搜集JSON消息
     *
     * @param chunk                    JSON消息快
     * @param chatHistoryStringBuilder 用于收集的字符串
     * @param seenTools                用于跟踪已经出现过的工具ID
     * @return
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, HashSet<String> seenTools) {
        //解析JSON消息快
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        //获取消息类型
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        //根据消息类型, 进行不同的处理
        switch (typeEnum) {
            //ai思考的消息, 直接拼接
            case PARTIAL_THINKING -> {
                PartialThinkingMessage partialThinkingMessage = JSONUtil.toBean(chunk, PartialThinkingMessage.class);
                String thinkingMessageTxt = partialThinkingMessage.getTxt();
                //添加消息
                chatHistoryStringBuilder.append(thinkingMessageTxt);
                return thinkingMessageTxt;
            }
            //AI响应的消息, 直接拼接
            case AI_RESPONSE -> {
                AiResponseMessage aiResponseMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String aiResponseMessageTxt = aiResponseMessage.getData();
                //添加消息
                chatHistoryStringBuilder.append(aiResponseMessageTxt);
                return aiResponseMessageTxt;
            }
            //工具调用的消息, 需要判断是否为第一次调用
            //不需要将工具的信息返回
            case TOOL_REQUEST -> {
                //TODO
                log.info("开始工具调用");
                //工具调用消息
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                //获取工具的id
                String toolId = toolRequestMessage.getId();
                //判断是否为第一次调用
                if (StrUtil.isNotBlank(toolId) && !seenTools.contains(toolId)) {
                    //第一调用该工具, 记录该功能并返回信息
                    //TODO  保存工具调用信息
                    log.info("第一次调用工具: {}", toolId);
                    seenTools.add(toolId);
                    //返回消息
                    BaseTool tool = toolManager.getTool(toolRequestMessage.getName());
                    log.info("工具调用成功: {}", toolRequestMessage.getName());
                    return tool.generateToolRequestResponse();
                } else {
                    //非第一次调用, 直接返回空字符串
                    return "";
                }

            }
            //工具调用完成后, 返回工具调用结果
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                //获取工具调用信息参数, 并转换为JSONObject 格式
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                //根据工具名称获取工具实例
                BaseTool tool = toolManager.getTool(toolExecutedMessage.getName());
                //调用工具
                String result = tool.generateToolExecutedResult(jsonObject);
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }
}

