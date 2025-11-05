package com.liuh.codegenerationbackend.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.liuh.codegenerationbackend.service.ProjectDownloadService;
import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.exception.ThrowUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

/**
 * @Description 下载服务实现
 */

@SuppressWarnings("all")
@Slf4j
@RestController
@RequestMapping("/projectDownload")
public class ProjectDownloadServiceImpl implements ProjectDownloadService {

    /**
     * 需要过滤的文件和目录名称
     */
    private static final Set<String> IGNORED_NAMES = Set.of(
            "node_modules",
            ".git",
            "dist",
            "build",
            ".DS_Store",
            ".env",
            "target",
            ".mvn",
            ".idea",
            ".vscode"
    );

    /**
     * 需要过滤的文件扩展名
     */
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(
            ".log",
            ".tmp",
            ".cache"
    );

    @Override
    public void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response) {
        //基础效验
        ThrowUtils.throwIf(StrUtil.isBlank(projectPath), ErrorCode.PARAMS_ERROR, "项目路径不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(downloadFileName), ErrorCode.PARAMS_ERROR, "下载文件名不能为空");
        File projectDir = new File(projectPath);
        ThrowUtils.throwIf(!projectDir.exists(), ErrorCode.PARAMS_ERROR, "项目路径不存在");
        ThrowUtils.throwIf(!projectDir.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径不是目录");
        log.info("开始下载项目: {} -> {}.zip", projectPath,  downloadFileName);


        //设置HTTP响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", downloadFileName)); //压缩的文件

        //定义文件过滤器
        FileFilter fileFilter = file -> isPathAllowed(projectDir.toPath(), file.toPath());
        //打包压缩
        try {
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false,fileFilter,projectDir );
            log.info("打包下载完成: {} -> {}.zip", projectPath,  downloadFileName);
        } catch (IOException e) {
            log.error("打包下载失败: {}", projectPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "打包下载失败");
        }
    }

    /**
     * 检查路径是否允许包含到压缩包中
     *
     * @param path     项目的根路径
     * @param fullPath 项目的完整路径
     * @return 是否允许
     */
    private boolean isPathAllowed(Path projectRoot, Path fullPath) {
        //得到相对路径 --  相对路径是相对于项目根路径的
        Path relativizePath = projectRoot.relativize(fullPath);
        //检查路径中的每一部分是否符合要求
        for (Path pathPart : relativizePath) {
            String partName = pathPart.toString();
            //检查是否在忽略列表中
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }
            //检查是否在忽略扩展名列表中
            if (IGNORED_NAMES.stream().anyMatch(ext -> partName.toLowerCase().endsWith(ext))) {
                return false;
            }
        }

        return true;
    }
}
