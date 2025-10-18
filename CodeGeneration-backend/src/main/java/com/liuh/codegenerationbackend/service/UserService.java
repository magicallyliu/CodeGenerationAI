package com.liuh.codegenerationbackend.service;


import com.liuh.codegenerationbackend.model.VO.user.LoginUserVO;
import com.liuh.codegenerationbackend.model.VO.user.UserVO;
import com.liuh.codegenerationbackend.model.dto.user.UserQueryRequest;
import com.liuh.codegenerationbackend.model.entity.User;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/magicallyliu">liuh</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * <p>
     * 账号长度不得低于4位
     * 密码不得低于8位
     * 密码和确认密码需要一致
     *
     * @param userAccount   注册用户名
     * @param userPassword  注册密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用于对用户密码的加密
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);


    /**
     * 获得登录后的用户脱敏信息
     *
     * @param user 需要脱敏的信息
     * @return 脱敏成功的信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     * @param userAccount  用户名
     * @param userPassword 密码
     * @param request      前端返回需要测试的用户名/密码
     * @return 脱敏后的用户信息 需要返回给前端
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户, 无须上传前端
     *
     * @param request 接收的信息
     * @return 数据
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    Boolean userLogout(HttpServletRequest request);


    /**
     * 获得用户脱敏信息
     *
     * @param user 需要脱敏的信息
     * @return 脱敏成功的信息
     */
    UserVO getUserVO(User user);

    /**
     * 获得多名用户脱敏信息
     *
     * @param userList 需要脱敏的用户列表
     * @return 脱敏成功的信息
     */
    List<UserVO> getUserVOList(List<User> userList);


    /**
     * 用户查询 -- 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);




//

//    /**
//     * 判断是否为管理员
//     *
//     * @param user
//     * @return
//     */
//    boolean isAdmin(User user);
}
