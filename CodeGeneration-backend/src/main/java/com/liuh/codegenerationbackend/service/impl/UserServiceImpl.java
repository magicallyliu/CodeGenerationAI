package com.liuh.codegenerationbackend.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.liuh.codegenerationbackend.constant.UserConstant;
import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.exception.ThrowUtils;
import com.liuh.codegenerationbackend.mapper.UserMapper;
import com.liuh.codegenerationbackend.model.VO.user.LoginUserVO;
import com.liuh.codegenerationbackend.model.VO.user.UserVO;
import com.liuh.codegenerationbackend.model.dto.user.UserQueryRequest;
import com.liuh.codegenerationbackend.model.entity.User;
import com.liuh.codegenerationbackend.model.enums.UserRoleEnum;
import com.liuh.codegenerationbackend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/magicallyliu">liuh</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        /*
        步骤
            1. 效验参数
            2. 检查用户账户是否和数据库中已有的重复
            3. 密码要加密
            4. 插入数据到数据库中
         */

        // 1. 效验参数,
        // 参数不能为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        //账号长度不得低于4位
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "用户账户过短");
        //密码不得低于8位
        ThrowUtils.throwIf(userPassword.length() < 8 || checkPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "用户密码过短");
        //密码和确认密码需要一致
        ThrowUtils.throwIf(!checkPassword.equals(userPassword),
                ErrorCode.PARAMS_ERROR, "两次输入密码不一致");

        // 2. 检查用户账户是否和数据库中已有的重复
        //通过 baseMapper 调用Mapper
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);

        long count = this.mapper.selectCountByQuery(queryWrapper);
        //判断是否有的条件是查询出的条数
        ThrowUtils.throwIf(count >= 1,
                ErrorCode.PARAMS_ERROR, "用户已存在");

        // 3. 密码要加密
        String encryptPassword = getEncryptPassword(userPassword);

        //4. 插入数据到数据库中
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        //设置用户的默认名称、用户角色
        user.setUserName(userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue());

        //用户的默认头像
        user.setUserAvatar("https://moment-gallery-1353804205.cos.ap-guangzhou.myqcloud.com/public/1920102552364191745/2025-10-16_swrpcoFJtNfeGvqg.webp");


        boolean saveResult = this.save(user);
        //为 true 代表插入数据库成功
        ThrowUtils.throwIf(!saveResult,
                ErrorCode.SYSTEM_ERROR, "注册失败, 插入数据库失败");


        return user.getId();
    }

    @Override
    public String getEncryptPassword(String userPassword) {
        //加盐, 混淆密码
        final String SALT = "liuGeneration";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (ObjUtil.isNull(user)) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        /*
            1. 效验
            2. 对前端传递的密码进行加密
            3. 查询数据中的数据是否存在
            4. 记录用户的登录状态
        */
        //1. 效验
        // 参数不能为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        //账号长度不得低于4位
        ThrowUtils.throwIf(userAccount.length() < 4,
                ErrorCode.PARAMS_ERROR, "用户账户格式错误");
        //密码不得低于8位
        ThrowUtils.throwIf(userPassword.length() < 8,
                ErrorCode.PARAMS_ERROR, "用户密码格式错误");

        //2. 对前端传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);

        //3. 查询数据中的数据是否存在
        //查询条件
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);

        User user = this.mapper.selectOneByQuery(queryWrapper);

        //判断账户是否存在
        if (ObjUtil.isNull(user) || !encryptPassword.equals(user.getUserPassword())) {
            log.info("user login failed: The account does not exist or Wrong password");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不存在 或 密码错误");
        }

        //4. 记录用户的登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
//        //保存用户空间登录信息, 使用 Sa-Token
//        //用户空间的权限调用
//        StpKit.SPACE.login(user.getId());
//        //将用户信息存入
//        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        //获取域中的数据
        User loginUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        //判断是否已经登录
        if (ObjUtil.isNull(loginUser) || ObjUtil.isNull(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        //检查用户id是否发生改变, 而不是一直使用缓存
        Long userId = loginUser.getId();
        //和数据库的数据进行比对
        User byId = this.getById(userId);
        //为空表示数据库中不存在
        if (ObjUtil.isNull(byId)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }

    @Override
    public Boolean userLogout(HttpServletRequest request) {
        //获取域中的数据
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        //判断是否已经登录
        if (ObjUtil.isNull(user)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户未登录");
        }

        //移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (ObjUtil.isNull(user)) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        //如果列表为空, 则返回空列表
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                .map(this::getUserVO)
                .collect(Collectors.toList()); //转换为其他形式
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .eq("userRole", userRole)
                .like("userAccount", userAccount)
                .like("userName", userName)
                .like("userProfile", userProfile)
                .orderBy(sortField, "ascend".equals(sortOrder));//  默认升序
    }

}
