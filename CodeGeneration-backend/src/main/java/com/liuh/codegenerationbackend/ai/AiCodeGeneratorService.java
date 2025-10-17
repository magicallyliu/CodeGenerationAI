package com.liuh.codegenerationbackend.ai;

import com.liuh.codegenerationbackend.ai.model.HtmlCodeResult;
import com.liuh.codegenerationbackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @Description
 */

@SuppressWarnings("all")
@Service
public interface AiCodeGeneratorService {

    /**
     * 生成 HTML 代码
     * @param userMessage 用户提示词
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成 多文件 代码
     * @param userMessage 用户提示词
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成 HTML 代码 -- 流式
     * @param userMessage 用户提示词
     * @return AI 的输出结果 --数据流
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成 多文件 代码 -- 流式
     * @param userMessage 用户提示词
     * @return AI 的输出结果 --数据流
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
