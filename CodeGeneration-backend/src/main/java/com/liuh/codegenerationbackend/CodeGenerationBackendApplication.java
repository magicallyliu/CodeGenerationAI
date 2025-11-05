package com.liuh.codegenerationbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.liuh.codegenerationbackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableCaching//开启缓存注解
public class CodeGenerationBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeGenerationBackendApplication.class, args);
    }

}
