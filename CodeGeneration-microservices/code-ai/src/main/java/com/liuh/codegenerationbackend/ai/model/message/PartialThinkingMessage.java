package com.liuh.codegenerationbackend.ai.model.message;

import dev.langchain4j.model.chat.response.PartialThinking;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Description ai 深度思考的内容
 */

@SuppressWarnings("all")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PartialThinkingMessage extends  StreamMessage {

    private String txt;

    public PartialThinkingMessage(PartialThinking partialThinking) {
        super(StreamMessageTypeEnum.PARTIAL_THINKING.getValue());
        this.txt = partialThinking.text();
    }
}
