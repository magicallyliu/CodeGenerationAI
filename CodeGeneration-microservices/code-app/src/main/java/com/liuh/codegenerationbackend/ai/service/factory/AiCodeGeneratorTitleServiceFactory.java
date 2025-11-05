package com.liuh.codegenerationbackend.ai.service.factory;

import com.liuh.codegenerationbackend.ai.service.AiCodeGeneratorTitleService;
import com.liuh.codegenerationbackend.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 */

@SuppressWarnings("all")
@Configuration
public class AiCodeGeneratorTitleServiceFactory {


    /**
     * 创建AI代码生成标题服务实例
     * 动态创建多个, 实现多例
     */
    public AiCodeGeneratorTitleService createAiCodeGeneratorTitleService() {
        //服用路由模型
        ChatModel titleChatModelPrototype = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGeneratorTitleService.class)
                .chatModel(titleChatModelPrototype)
                .build();
    }

    /**
     * 创建AI代码生成标题服务实例
     * 动态创建多个, 实现多例
     */
    @Bean
    public AiCodeGeneratorTitleService aiCodeGeneratorTitleService() {
        return  createAiCodeGeneratorTitleService();
    }
}
