package com.liuh.codemodel.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 用于前后端数据交换 -- 用户登录
 */

@SuppressWarnings("all")
@Data
public class UserLoginRequest implements Serializable {

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    private static final long serialVersionUID = 1L;
}
