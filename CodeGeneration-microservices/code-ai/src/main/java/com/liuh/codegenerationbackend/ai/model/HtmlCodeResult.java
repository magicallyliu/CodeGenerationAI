package com.liuh.codegenerationbackend.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @Description 生成单 Html 文件的返回结果
 */

@SuppressWarnings("all")
@Data
@Description("生成单 Html 文件的返回结果")
public class HtmlCodeResult {

    /**
     * Html 代码
     */
    @Description("Html 代码")
    private String htmlCode;

    /**
     * 描述
     */
    @Description("生成的代码描述")
    private  String description;
}
