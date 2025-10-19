package com.liuh.codegenerationbackend.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liuh.codegenerationbackend.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @Description AI服务创建工厂
 */

@SuppressWarnings("all")
@Configuration
@Slf4j
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
    private StreamingChatModel streamingChatModel;


    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService  chatHistoryService;
    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> { //当key被强行移除时，会触发该监听器
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();


    /**
     * 根据 appId 获取服务（带缓存）
     * 直接通过cofeeeine缓存获取对话记忆
     * 若没有, 则去创建新的对话记忆
     *
     *  @param appId 应用id
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建新的 AI 服务实例
     * 会根据appId创建独立的对话记忆
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore) // redis配置信息
                .maxMessages(20)
                .build();
        //从数据库中获取历史记录, 加载到对话记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }

    /**
     * 创建 AI 代码生成器服务
     * 在项目启动时, 会自动创建 AI 代码生成器服务
     */
    public AiCodeGeneratorService aiCodeGeneratorService() {
        //基于接口, 展示 实现类
        //1. 需要实现的接口. 2.  AI 模型
        return getAiCodeGeneratorService(0L);
    }
}



