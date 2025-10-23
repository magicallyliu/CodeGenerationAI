package com.liuh.codegenerationbackend.ai.service.factory;

import com.liuh.codegenerationbackend.ai.service.AiCodeGeneratorTitleService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 */

@SuppressWarnings("all")
@Configuration
public class AiCodeGeneratorTitleServiceFactory {

    @Resource
    private ChatModel chatModel;

    /**
     * 创建AI代码生成标题服务实例
     */
    @Bean
    public AiCodeGeneratorTitleService aiCodeGeneratorTitleService() {
        return AiServices.builder(AiCodeGeneratorTitleService.class)
                .chatModel(chatModel)
                .build();
    }
}
