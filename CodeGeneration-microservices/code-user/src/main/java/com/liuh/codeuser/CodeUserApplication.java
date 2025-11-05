package com.liuh.codeuser;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Description
 */

@SuppressWarnings("all")
@SpringBootApplication
@MapperScan("com.liuh.codeuser.mapper")
@ComponentScan("com.liuh")
@EnableDubbo
public class CodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeUserApplication.class, args);
    }
}
