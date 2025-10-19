package com.liuh.codegenerationbackend;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.liuh.codegenerationbackend.mapper")
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})// 禁止自动配置 RedisEmbeddingStoreAutoConfiguration
@EnableAspectJAutoProxy(exposeProxy = true)
public class CodeGenerationBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeGenerationBackendApplication.class, args);
    }

}
