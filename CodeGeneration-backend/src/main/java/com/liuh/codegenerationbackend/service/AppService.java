package com.liuh.codegenerationbackend.service;

import cn.hutool.core.bean.BeanUtil;
import com.liuh.codegenerationbackend.model.VO.app.AppVO;
import com.liuh.codegenerationbackend.model.VO.user.UserVO;
import com.liuh.codegenerationbackend.model.dto.app.AppQueryRequest;
import com.liuh.codegenerationbackend.model.entity.User;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.liuh.codegenerationbackend.model.entity.App;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 *
 * @author <a href="https://github.com/magicallyliu">liuh</a>
 */
public interface AppService extends IService<App> {


    /**
     * 构造应用查询条件
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 获得脱敏后的应用信息
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 批量获取脱敏后的应用信息
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);


    /**
     * 通过聊天生成应用代码
     *
     * @param appId     应用id
     * @param message   当前聊天的用户提示词
     * @param loginUser 登录用户
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 应用部署
     *
     * @param appId     应用id
     * @param loginUser 登录用户
     * @return 返回部署的地址
     */
    String deployApp(Long appId, User loginUser);
}

