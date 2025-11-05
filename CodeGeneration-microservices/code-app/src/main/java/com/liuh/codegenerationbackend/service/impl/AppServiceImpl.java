package com.liuh.codegenerationbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.liuh.codegenerationbackend.service.AppService;
import com.liuh.codegenerationbackend.service.ChatHistoryService;
import com.liuh.codegenerationbackend.innerservice.InnerScreenshotService;
import com.liuh.codegenerationbackend.innerservice.InnerUserService;
import com.liuh.codegenerationbackend.ai.service.AiCodeGenTypeRoutingService;
import com.liuh.codegenerationbackend.ai.service.AiCodeGeneratorTitleService;
import com.liuh.codegenerationbackend.ai.service.factory.AiCodeGenTypeRoutingServiceFactory;
import com.liuh.codegenerationbackend.ai.service.factory.AiCodeGeneratorTitleServiceFactory;
import com.liuh.codegenerationbackend.constant.AppConstant;
import com.liuh.codegenerationbackend.core.AiCodeGeneratorFacade;
import com.liuh.codegenerationbackend.core.builder.VueProjectBuilder;
import com.liuh.codegenerationbackend.core.handler.StreamHandlerExecutor;
import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.exception.ThrowUtils;
import com.liuh.codegenerationbackend.model.VO.app.AppVO;
import com.liuh.codegenerationbackend.model.VO.user.UserVO;
import com.liuh.codegenerationbackend.model.dto.app.AppAddRequest;
import com.liuh.codegenerationbackend.model.dto.app.AppQueryRequest;
import com.liuh.codegenerationbackend.model.entity.User;
import com.liuh.codegenerationbackend.model.enums.ChatHistoryMessageTypeEnum;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;

import java.lang.Thread;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.liuh.codegenerationbackend.model.entity.App;
import com.liuh.codegenerationbackend.mapper.AppMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
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
@Slf4j
@RestController
@RequestMapping("/app")
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @DubboReference
    private InnerUserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @DubboReference
    private InnerScreenshotService screenshotService;


    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Resource
    private AiCodeGeneratorTitleServiceFactory aiCodeGeneratorTitleServiceFactory;

    @Resource
    private ProjectDownloadServiceImpl projectDownloadService;

    @Override
    public Long addApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");

        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 根据ai智能生成应用名称
        //每次调用使用新的AiCodeGeneratorTitleService，实现多例
        AiCodeGeneratorTitleService aiCodeGeneratorTitleService = aiCodeGeneratorTitleServiceFactory.createAiCodeGeneratorTitleService();
        String generateTitle = aiCodeGeneratorTitleService.generateTitle(initPrompt);
        app.setAppName(generateTitle);
        // 使用ai智能选择代码生成类型
        //每次调用使用新的AiCodeGenTypeRoutingService，实现多例
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(codeGenTypeEnum.getValue());
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }


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

        //5. 在调用ai生成代码之前, 保存用户消息到数据库中
        boolean addChatMessage = chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser);
        ThrowUtils.throwIf(!addChatMessage, ErrorCode.SYSTEM_ERROR, "保存用户消息失败");

        //6. 调用 AI 生成代码(流式)
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateSaveCodeStream(message, codeGenTypeEnum, app.getId());

        //7. 搜集 AI 响应的内容, 并且在完成对话后, 保存到对话历史中
        return streamHandlerExecutor.doExecute(contentFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum);
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
                App existApp = this.getOne(QueryWrapper.create().eq("deployKey", deployKey));
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


        //7.-1 vue项目特殊处理, 执行构建
        //将代码类型转化为枚举
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (CodeGenTypeEnum.VUE_PROJECT.equals(codeGenTypeEnum)) {
            //执行构建
            boolean buildProject = vueProjectBuilder.buildProject(dirUrl);
            ThrowUtils.throwIf(!buildProject, ErrorCode.SYSTEM_ERROR, "应用部署失败, 请重新尝试");
            //需要将构建后的文件复制到部署目录
            // 检查 dist 目录是否存在
            File distDir = new File(dirUrl, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 构建完成后，需要将构建后的文件复制到部署目录
            dirFile = distDir;
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
        String appDeployUrl = String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);

        //10 异步生成截图 , 更新应用封面
        generateAppScreenshot(appId, appDeployUrl);

        //11. 返回部署地址
        return appDeployUrl;

    }

    /**
     * 异步生成截图
     */
    @Override
    public void generateAppScreenshot(Long appId, String appDeployUrl) {
        Thread.startVirtualThread(() -> {
            //截图
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appDeployUrl);
            //更新应用封面
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updateResult = this.updateById(updateApp);
            ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用封面失败");
        });
    }


    /**
     * 根据数据主键删除数据。
     * 形参:
     * id – 数据主键
     * 返回值:
     * true 删除成功，false 删除失败。
     * <p>
     * 覆盖方法,  删除应用时, 需要删除应用下的所有对话历史
     *
     * @param id 应用id
     * @return true 删除成功，false 删除失败。
     */
    @Override
    public boolean removeById(Serializable id) {
        //使用事务删除
        //1. 判断
        //转换格式
        //关联删除应用
        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            //1. 判断
            if (ObjUtil.isNull(id)) {
                return false;
            }
            //转换格式
            long appId = Long.parseLong(id.toString());
            if (appId <= 0) {
                return false;
            }
            //关联删除应用
            boolean chatDeleteResult = chatHistoryService.deleteByAppId(appId);
            if (!chatDeleteResult) {
                return false;
            }
            return super.removeById(id);
        }));
    }


    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param request  请求
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        // 1. 基础校验
        ThrowUtils.throwIf(ObjUtil.isNull(appId) || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 2. 查询应用信息
        App app = getById(appId);
        ThrowUtils.throwIf(ObjUtil.isNull(app), ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验：只有应用创建者可以下载代码
        User loginUser = InnerUserService.getLoginUser(request);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
        }
        // 4. 构建应用代码目录路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 5. 检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
        // 6. 生成下载文件名（不建议添加中文内容）
        String downloadFileName = String.valueOf(appId);
        // 7. 调用通用下载服务
        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
    }

}
