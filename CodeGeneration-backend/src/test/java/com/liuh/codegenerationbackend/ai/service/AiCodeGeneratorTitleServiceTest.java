package com.liuh.codegenerationbackend.ai.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description
 */

@SuppressWarnings("all")
@SpringBootTest
@Slf4j
class AiCodeGeneratorTitleServiceTest {
    @Resource
    private AiCodeGeneratorTitleService aiCodeGeneratorTitleService;

    @Test
    void generateTitle() {
        String userPrompt = "做一个电商管理系统，包含用户管理、商品管理、订单管理，需要路由和状态管理";
        String generateTitle = aiCodeGeneratorTitleService.generateTitle(userPrompt);
        System.out.println(generateTitle);
        log.info("用户需求: {} -> {}", userPrompt, generateTitle);
    }
}