package com.liuh.codeclient.innerservice;

import cn.hutool.core.util.ObjUtil;
import com.liuh.codecommom.exception.BusinessException;
import com.liuh.codecommom.exception.ErrorCode;
import com.liuh.codemodel.model.VO.user.UserVO;
import com.liuh.codemodel.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.liuh.codecommom.constant.UserConstant.USER_LOGIN_STATE;

/**
 *  @Description 内部服务类，用于内部调用
 */
public interface InnerUserService {

    /**
     * 根据数据主键查询数据集合。
     *
     * @param ids 数据主键
     * @return 数据集合
     */
    List<User> listByIds(Collection<? extends Serializable> ids);

    /**
     * 根据数据主键查询一条数据。
     * @param id 数据主键
     * @return 查询结果信息
     */
    User getById(Serializable id);

    /**
     * 获得用户脱敏信息
     *
     * @param user 需要脱敏的信息
     * @return 脱敏成功的信息
     */
    UserVO getUserVO(User user);

    /**
     *  获取当前登录用户
     *  静态方法，避免跨服务调用
     * @param request
     * @return
     */
    static User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (ObjUtil.isNull(currentUser) || ObjUtil.isNull(currentUser.getId())) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }
}
