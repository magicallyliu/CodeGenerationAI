package com.liuh.codegenerationbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class CodeGenerationBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeGenerationBackendApplication.class, args);
    }

}
