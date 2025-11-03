package com.liuh.codemodel.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 创建应用 , 只需要提示词
 */

@SuppressWarnings("all")
@Data
public class AppAddRequest implements Serializable {

    /**
     * 提示词
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}
