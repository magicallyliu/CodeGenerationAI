package com.liuh.codegenerationbackend.core;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.liuh.codegenerationbackend.ai.service.AiCodeGeneratorService;
import com.liuh.codegenerationbackend.ai.service.factory.AiCodeGeneratorServiceFactory;
import com.liuh.codegenerationbackend.ai.model.HtmlCodeResult;
import com.liuh.codegenerationbackend.ai.model.MultiFileCodeResult;
import com.liuh.codegenerationbackend.ai.model.message.AiResponseMessage;
import com.liuh.codegenerationbackend.ai.model.message.PartialThinkingMessage;
import com.liuh.codegenerationbackend.ai.model.message.ToolExecutedMessage;
import com.liuh.codegenerationbackend.ai.model.message.ToolRequestMessage;
import com.liuh.codegenerationbackend.constant.AppConstant;
import com.liuh.codegenerationbackend.core.builder.VueProjectBuilder;
import com.liuh.codegenerationbackend.core.parser.CodeParserExector;
import com.liuh.codegenerationbackend.core.save.CodeFileSaveExector;
import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * @Description Ai代码生成门面类, 组合代码生成和保存功能
 */

@SuppressWarnings("all")
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder  vueProjectBuilder;


    /**
     * 门面的入口, 根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用id
     * @return 返回生成的代码文件
     */
    public File generateSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (ObjUtil.isEmpty(codeGenTypeEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }

        //根据新的appId获取对应的AI服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        //根据类型生成并保存代码
        return switch (codeGenTypeEnum) {
            case HTML -> {
                //调用ai生成
                HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
                //保存代码
                yield CodeFileSaveExector.executeSave(htmlCodeResult, codeGenTypeEnum, appId);
            }

            case MULTI_FILE -> {
                //调用ai生成
                MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                //保存代码
                yield CodeFileSaveExector.executeSave(multiFileCodeResult, codeGenTypeEnum, appId);
            }
            default -> {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持该类型的代码生成: " + codeGenTypeEnum.getValue());
            }
        };
    }


    /**
     * 门面的入口, 根据类型生成并保存代码 -- 流式
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用id
     * @return 返回生成的代码文件
     */
    public Flux<String> generateSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        if (ObjUtil.isEmpty(codeGenTypeEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码生成类型不能为空");
        }

        //根据新的appId获取对应的AI服务
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);

        //根据类型生成/解析/并保存代码
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appId);
            }

            case MULTI_FILE -> {
                Flux<String> codeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId, userMessage);
                yield processTokenStream(tokenStream, appId);
            }
            default -> {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持该类型的代码生成: " + codeGenTypeEnum.getValue());
            }

        };
    }

    /**
     * 将tokenStream 转换为 Flux<String>
     *
     * @param tokenStream
     * @return
     */
    private Flux<String> processTokenStream(TokenStream tokenStream, Long appId) {
        //使用ai返回的tokenStream流, 构造一条流
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    //ai深度思考
                    .onPartialThinking((PartialThinking partialThinking) -> {
                        PartialThinkingMessage partialThinkingMessage = new PartialThinkingMessage(partialThinking);
                        sink.next(JSONUtil.toJsonStr(partialThinkingMessage));
                    })
                    //ai工具调用
                    .beforeToolExecution((BeforeToolExecution beforeToolExecution) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(beforeToolExecution);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    //工具执行结果
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    //结束流的创建
                    .onCompleteResponse((ChatResponse response) -> {
                        //当所有的流式返回后, 执行构建流程
                        //使用同步打包
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete();
                    })
                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();//开始监听
        });
    }


    /**
     * 生成并保存代码(抽象出公共逻辑) -- 流式()
     * HTML/多文件/vue工程
     *
     * @param codeStream      代码流
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用id
     * @return 返回生成的代码文件--流式
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        //定义一个字符串拼接器, 用于当流式返回所有代码之后再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            //实时搜集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            //当流式返回所有代码之后再保存代码
            try {
                String completeCode = codeBuilder.toString();
                //使用执行器解析代码
                Object parserResult = CodeParserExector.executeParser(completeCode, codeGenTypeEnum);
                //使用执行器保存代码
                File saveFile = CodeFileSaveExector.executeSave(parserResult, codeGenTypeEnum, appId);
                log.info("多文件代码保存成功, 保存路径: {}", saveFile.getAbsolutePath());
            } catch (Exception e) {
                log.error("多文件代码保存失败", e.getMessage());
            }
        });
    }

}
