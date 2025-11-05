package com.liuh.codegenerationbackend.ai.service.factory;

import com.liuh.codegenerationbackend.ai.service.AiCodeGenTypeRoutingService;
import com.liuh.codegenerationbackend.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description 根据用户需求智能选择代码类型
 */

@SuppressWarnings("all")
@Configuration
public class AiCodeGenTypeRoutingServiceFactory {

    /**
     * 创建AI代码生成类型路由服务实例
     * 同时也需要动态创建多个
     */
    public AiCodeGenTypeRoutingService createAiCodeGenTypeRoutingService() {
        //每次调用获取新的模型
        ChatModel routingChatModelPrototype = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiCodeGenTypeRoutingService.class)
                .chatModel(routingChatModelPrototype)
                .build();
    }

    /**
     * 创建AI代码生成类型路由服务实例
     * 用以兼容
     */
    @Bean
    public AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService() {
        return createAiCodeGenTypeRoutingService();
    }
}
