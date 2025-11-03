package com.liuh.codemodel.model.VO.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 用户更新 -- 用户
 */

@SuppressWarnings("all")
@Data
public class UserUpdateRequestVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;


    private static final long serialVersionUID = 1L;
}
