package com.liuh.codegenerationbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.liuh.codegenerationbackend.constant.AppConstant;
import com.liuh.codegenerationbackend.core.AiCodeGeneratorFacade;
import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.exception.ThrowUtils;
import com.liuh.codegenerationbackend.model.VO.app.AppVO;
import com.liuh.codegenerationbackend.model.VO.user.UserVO;
import com.liuh.codegenerationbackend.model.dto.app.AppQueryRequest;
import com.liuh.codegenerationbackend.model.entity.User;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;
import com.liuh.codegenerationbackend.service.AppService;
import com.liuh.codegenerationbackend.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.liuh.codegenerationbackend.model.entity.App;
import com.liuh.codegenerationbackend.mapper.AppMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/magicallyliu">liuh</a>
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (ObjUtil.isNull(appQueryRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id, ObjUtil.isNotEmpty(id))
                .like("appName", appName, ObjUtil.isNotEmpty(appName))
                .like("cover", cover, ObjUtil.isNotEmpty(cover))
                .like("initPrompt", initPrompt, ObjUtil.isNotEmpty(initPrompt))
                .eq("codeGenType", codeGenType, ObjUtil.isNotEmpty(codeGenType))
                .eq("deployKey", deployKey, ObjUtil.isNotEmpty(deployKey))
                .eq("priority", priority, ObjUtil.isNotEmpty(priority))
                .eq("userId", userId, ObjUtil.isNotEmpty(userId))
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public AppVO getAppVO(App app) {
        if (ObjUtil.isNull(app)) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        //Map: User::getId, userService::getUserVO
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        //1. 参数效验
        ThrowUtils.throwIf(ObjUtil.isNull(appId), ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息不能为空");
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.PARAMS_ERROR, "用户不能为空");

        //2. 查询应用信息
        //获取应用
        App app = getById(appId);
        ThrowUtils.throwIf(ObjUtil.isNull(app), ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        //3. 权限效验, 仅本人可以和自己的应用对话
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限");

        //4. 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(ObjUtil.isNull(codeGenTypeEnum), ErrorCode.PARAMS_ERROR, "应用类型错误");

        //5. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generateSaveCodeStream(message, codeGenTypeEnum, app.getId());
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        //1. 参数效验
        ThrowUtils.throwIf(ObjUtil.isNull(appId), ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(ObjUtil.isNull(loginUser), ErrorCode.PARAMS_ERROR, "用户不能为空");

        //2. 查询应用信息
        //获取应用
        App app = getById(appId);
        ThrowUtils.throwIf(ObjUtil.isNull(app), ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        //3. 权限效验, 仅本人可以部署自己的应用
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限部署应用");

        //4. 检查是否已经有部署 deployKey
        //没有则生成 6位 deployKey 字母+数字
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            //重复生成 deployKey, 直到不重复为止
            while (true) {
                deployKey = RandomUtil.randomString(6);
                //查询是否已经存在
                App existApp = this.getOne( QueryWrapper.create().eq("deployKey", deployKey));
                if (ObjUtil.isNull(existApp)) {
                    //不存在, 则可以生成
                    break;
                }
            }
        }

        //5. 获取代码生成类型, 获取原始代码生成位置
        String codeGenType = app.getCodeGenType();
        String dirName = codeGenType + "_" + appId;
        String dirUrl = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + dirName;

        //6. 检查路径是否存在
        File dirFile = new File(dirUrl);
        //如果不存在或者不是目录
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用代码不存在, 请先生成应用");
        }

        //7. 复制文件到部署目录
        String deployDirUrl = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(dirFile, new File(deployDirUrl), true);
        } catch (IORuntimeException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败");
        }

        //8. 更新数据库的部署相关内容
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");

        //9. 生成可访问的 url 地址
        return String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }
}
