package com.liuh.codegenerationbackend.ai.service;

import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;
import org.springframework.stereotype.Service;

/**
 * @Description AI  代码生成类型路由服务
 * 使用结构化输出直接返回枚举类型
 */

@SuppressWarnings("all")
@Service
public interface AiCodeGenTypeRoutingService {

    /**
     * 根据需求智能选择代码类型
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
    CodeGenTypeEnum routeCodeGenType(String userMessage);
}

