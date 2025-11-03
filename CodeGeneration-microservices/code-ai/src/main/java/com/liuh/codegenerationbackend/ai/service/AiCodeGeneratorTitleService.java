package com.liuh.codegenerationbackend.ai.service;

import dev.langchain4j.service.SystemMessage;
import org.springframework.stereotype.Service;

/**
 * @Description 根据用户的提示词智能生成代码标题
 */

@SuppressWarnings("all")
@Service
public interface AiCodeGeneratorTitleService {

    @SystemMessage(fromResource = "prompt/codegen-title-system-prompt.txt")
    String generateTitle(String userMessage);
}
