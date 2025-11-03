package com.liuh.codegenerationbackend.ai.guardrail;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;

/**
 * 输出护轨
 * 用于检查 AI 生成的响应是否合法，如果响应不合法，则会重新生成响应。
 */
public class RetryOutputGuardrail implements OutputGuardrail {

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String response = responseFromLLM.text();
        // 检查响应是否为空或过短
        if (response == null || response.trim().isEmpty()) {
            return reprompt("响应内容为空", "请重新生成完整的内容");
        }
        if (response.trim().length() < 10) {
            return reprompt("响应内容过短", "请提供更详细的内容");
        }
        // 检查是否包含敏感信息或不当内容
        String containsSensitiveContent = containsSensitiveContent(response);
        if (StrUtil.isNotBlank(containsSensitiveContent)) {
            return reprompt("包含敏感信息", "请重新生成内容，避免包含敏感信息, 敏感信息为" +  containsSensitiveContent);
        }
        return success();
    }
    
    /**
     * 检查是否包含敏感内容
     */
    private String containsSensitiveContent(String response) {
        String lowerResponse = response.toLowerCase();
        String[] sensitiveWords = {
            "密码", "password", "secret", "token", 
            "api key", "私钥", "证书", "credential"
        };
        for (String word : sensitiveWords) {
            if (lowerResponse.contains(word)) {
                return word;
            }
        }
        return null;
    }
}
