package com.liuh.codemodel.model.dto.app;

import lombok.Data;

/**
 * @Description 应用部署请求
 */

@SuppressWarnings("all")
@Data
public class AddDeployRequest {

    /**
     * 应用id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}
