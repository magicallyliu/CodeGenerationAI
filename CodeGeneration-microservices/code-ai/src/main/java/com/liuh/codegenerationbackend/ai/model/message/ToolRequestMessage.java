package com.liuh.codegenerationbackend.ai.model.message;

import dev.langchain4j.service.tool.BeforeToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Description  工具调用消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ToolRequestMessage extends StreamMessage {

    private String id;

    private String name;

    /**
     *  工具调用参数信息
     */
    private String arguments;

    /**
     *
     * @param beforeToolExecution 调用ai得到的工具调用信息--请求
     */
    public ToolRequestMessage(BeforeToolExecution beforeToolExecution) {
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        this.id = beforeToolExecution.request().id();
        this.name = beforeToolExecution.request().name();
        this.arguments = beforeToolExecution.request().arguments();
    }
}
