package com.liuh.codegenerationbackend;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @Description
 */

@SuppressWarnings("all")
@MapperScan("com.liuh.codegenerationbackend.mapper")
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})// 禁止自动配置 RedisEmbeddingStoreAutoConfiguration
@EnableCaching//开启缓存注解
@EnableDubbo
public class CodeAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeAppApplication.class, args);
    }

}
