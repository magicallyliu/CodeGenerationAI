package com.liuh.codegenerationbackend.langgraph4j.ai;

import com.liuh.codegenerationbackend.langgraph4j.model.ImageResource;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import java.util.List;

/**
 * @Description 图片搜集 ai 服务接口
 */

@SuppressWarnings("all")

public interface ImageCollectionService {


    /**
     * 根据用户提示词收集所需的图片资源
     * ai根据需求调用合适的工具
     */
    @SystemMessage(fromResource = "prompt/image-collection-system-prompt.txt")
    String collectImages(@UserMessage String userPrompt);
}
