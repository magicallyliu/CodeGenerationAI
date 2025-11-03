package com.liuh.codegenerationbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 用于前后端数据交换 -- 用户注册
 */

@SuppressWarnings("all")
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 4884086457375466192L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;
}
