package com.liuh.codegenerationbackend.ai.service.factory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.liuh.codegenerationbackend.ai.service.AiCodeGeneratorService;
import com.liuh.codegenerationbackend.ai.tools.*;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;
import com.liuh.codegenerationbackend.service.ChatHistoryService;
import com.liuh.codegenerationbackend.utils.SpringContextUtil;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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
     *采用默认的模型
     */
    @Resource(name = "openAiChatModel")
    private ChatModel chatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ToolManager toolManager;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> { //当key被强行移除时，会触发该监听器
                log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();


    /**
     * 根据 appId 获取服务（带缓存）
     * 直接通过cofeeeine缓存获取对话记忆
     * 若没有, 则去创建新的对话记忆 (为兼容原本的逻辑)
     *
     * @param appId 应用id
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.MULTI_FILE);
    }

    /**
     * 根据 appId 获取服务（带缓存）
     * 直接通过cofeeeine缓存获取对话记忆
     * 若没有, 则去创建新的对话记忆
     *
     * @param appId           应用id
     * @param codeGenTypeEnum 代码生成类型
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
        //设置缓存的key\
        String cacheKey = buildCacheKey(appId, codeGenTypeEnum);
        return serviceCache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenTypeEnum));
    }

    /**
     * 创建新的 AI 服务实例
     * 会根据appId创建独立的对话记忆
     *
     * @param appId           应用id
     * @param codeGenTypeEnum 代码生成类型
     * @return AiCodeGeneratorService
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenTypeEnum) {
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

        return switch (codeGenTypeEnum) {
            case VUE_PROJECT -> {
                //获取模型的bean(StreamingChatModel), 每次调用使用新的, 以解决并发问题

                StreamingChatModel reasoningStreamingChatModelPrototype = SpringContextUtil.getBean("reasoningStreamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)

                        .chatModel(chatModel)
                        //每次调用都使用新的chatModel对象
                        .streamingChatModel(reasoningStreamingChatModelPrototype)
                        //根据不同的对话id, 创建不同的对话记忆//但是此处仅为ai调用时传入的appId服务, 不为创建对话服务
                        //因为 ai 生成工具时, 以会话记忆的形式返回历史成功, 所有需要将会话记忆上线增加, 防止出现 重新生成代码
                        .chatMemoryProvider(memoryId -> chatMemory.withMaxMessages(55))
                        //设置ai连续调用工具的上线, 超过上线会强制结束
                        .maxSequentialToolsInvocations(100)
                        .tools(toolManager.getAllTools())
                        .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                ToolExecutionResultMessage.from(toolExecutionRequest,
                                        "Error: there is no tool called " + toolExecutionRequest.name())
                        )//当ai出现幻觉时, 会调用此方法. 即ai想调用其他工具时, 让ai重新执行, 放弃不存在的工具的调用
                        .build();
            }


            case HTML, MULTI_FILE -> {
                //获取模型的bean(StreamingChatModel), 每次调用使用新的, 以解决并发问题
                StreamingChatModel streamingChatModelPrototype = SpringContextUtil.getBean("streamingChatModelPrototype", StreamingChatModel.class);
                yield AiServices.builder(AiCodeGeneratorService.class)
                        .chatModel(chatModel)
                        //采用新的chatModel对象
                        .streamingChatModel(streamingChatModelPrototype)
                        .chatMemory(chatMemory)//对应不同app设置不同的对话记忆
                        .build();
            }

        };
    }

    /**
     * 根据 appId 和 代码生成类型, 获取缓存key
     *
     * @param appId
     * @param codeGenTypeEnum
     * @return
     */
    private String buildCacheKey(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return appId + "_" + codeGenTypeEnum.name();
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



