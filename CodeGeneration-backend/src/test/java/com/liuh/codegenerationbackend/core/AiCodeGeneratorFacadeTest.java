package com.liuh.codegenerationbackend.core;

import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;


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
        File file = aiCodeGeneratorFacade.generateSaveCode("生成一个用户信息修改页面, 包含头像, 昵称, 简介", CodeGenTypeEnum.HTML,1L);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateSaveCodeStream("生成一个用户信息修改页面, 包含头像, 昵称, 简介. 不超过20行代码", CodeGenTypeEnum.MULTI_FILE,1L);
        //等待数据完成
        List<String> result = codeStream.collectList().block();
        Assertions.assertNotNull(codeStream);
        //凭借
        String join = String.join("\n", result);
        Assertions.assertNotNull(join);

    }
}