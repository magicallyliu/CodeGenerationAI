package com.liuh.codegenerationbackend.ai.service;

import com.liuh.codegenerationbackend.ai.model.HtmlCodeResult;
import com.liuh.codegenerationbackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @Description
 */

@SuppressWarnings("all")
@Service
public interface AiCodeGeneratorService {

    /**
     * 单HTML提示词
     */
    @Resource
    static final String HTML_SYSTEM_PROMPT_FILE = "prompt/codegen-html-system-prompt.txt";

    /**
     * 多文件提示词
     */
    @Resource
    static final String MULTI_FILE_SYSTEM_PROMPT_FILE = "prompt/codegen-multi-file-system-prompt.txt";

    /**
     * vue 项目代码提示词
     */
    @Resource
    static final String VUE_PROJECT_SYSTEM_PROMPT_FILE = "prompt/codegen-vue-project-system-prompt.txt";


    /**
     * 生成 HTML 代码
     *
     * @param userMessage 用户提示词
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = HTML_SYSTEM_PROMPT_FILE)
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成 多文件 代码
     *
     * @param userMessage 用户提示词
     * @return AI 的输出结果
     */
    @SystemMessage(fromResource = MULTI_FILE_SYSTEM_PROMPT_FILE)
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成 HTML 代码 -- 流式
     *
     * @param userMessage 用户提示词
     * @return AI 的输出结果 --数据流
     */
    @SystemMessage(fromResource = HTML_SYSTEM_PROMPT_FILE)
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成 多文件 代码 -- 流式
     *
     * @param userMessage 用户提示词
     * @return AI 的输出结果 --数据流
     */
    @SystemMessage(fromResource = MULTI_FILE_SYSTEM_PROMPT_FILE)
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成 vue项目 代码 -- 流式
     *
     * @param appId       应用id -- 需要调用给工具类
     * @param userMessage 用户提示词
     * @return AI 的输出结果 --数据流
     */
    @SystemMessage(fromResource = VUE_PROJECT_SYSTEM_PROMPT_FILE)
    TokenStream generateVueProjectCodeStream(@MemoryId Long appId, @UserMessage String userMessage);

}
