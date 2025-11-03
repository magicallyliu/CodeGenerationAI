package com.liuh.codemodel.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 更新应用
 */

@SuppressWarnings("all")
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}

