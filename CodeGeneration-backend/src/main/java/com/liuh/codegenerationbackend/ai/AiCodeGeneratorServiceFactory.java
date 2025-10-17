package com.liuh.codegenerationbackend.ai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description AI服务创建工厂
 */

@SuppressWarnings("all")
@Configuration
public class AiCodeGeneratorServiceFactory {

    /**
     * 选用的ai模型
     */
    @Resource
    private ChatModel chatModel;

    /**
     * 流式的ai模型
     */
    @Resource
    private StreamingChatModel  streamingChatModel;

    /**
     * 创建 AI 代码生成器服务
     * 在项目启动时, 会自动创建 AI 代码生成器服务
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        //基于接口, 展示 实现类
        //1. 需要实现的接口. 2.  AI 模型
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)//绑定流式模型
                .build();
    }
}
