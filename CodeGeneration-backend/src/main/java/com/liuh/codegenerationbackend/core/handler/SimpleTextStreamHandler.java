package com.liuh.codegenerationbackend.core.handler;

import com.liuh.codegenerationbackend.model.entity.User;
import com.liuh.codegenerationbackend.model.enums.ChatHistoryMessageTypeEnum;
import com.liuh.codegenerationbackend.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @Description 原生文本流处理器
 */

@SuppressWarnings("all")
@Slf4j
@Component
public class SimpleTextStreamHandler {

    /**
     * 处理文本流
     * 搜集 AI 响应的内容, 并且在完成对话后, 保存到对话历史中
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
        StringBuilder aiResponseBuiler = new StringBuilder();
        return originFlux.map(content -> {
            //实时搜集响应的内容
            aiResponseBuiler.append(content);
            return content;
        }).doOnComplete(() -> {
            //流式返回结束后, 保存对话记忆
            chatHistoryService.addChatMessage(appId, aiResponseBuiler.toString(),
                    ChatHistoryMessageTypeEnum.AI.getValue(), loginUser);
        }).doOnError(error -> {
            //即使流式返回失败, 也需要将消息记录到数据库中
            String errorMessage = "AI 回复消息失败" + error.getMessage();
            chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser);
        });
    }
}
