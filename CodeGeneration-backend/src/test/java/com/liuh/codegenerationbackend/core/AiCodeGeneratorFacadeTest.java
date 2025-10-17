package com.liuh.codegenerationbackend.core;

import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;


/**
 * @Description
 */

@SuppressWarnings("all")
@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateSaveCode() {
        File file = aiCodeGeneratorFacade.generateSaveCode("生成一个用户信息修改页面, 包含头像, 昵称, 简介", CodeGenTypeEnum.HTML);
        Assertions.assertNotNull(file);
    }
}