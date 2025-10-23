package com.liuh.codegenerationbackend.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Description 下载服务
 */

@SuppressWarnings("all")

public interface ProjectDownloadService {

    /**
     * 下载项目, 格式为 zip
     *
     * @param projectPath      项目路径
     * @param downloadFileName 下载文件名
     * @param response         HttpServletResponse 响应头, 需要修改吗以返回给前端
     * @return
     */
    void downloadProjectAsZip(String projectPath, String downloadFileName, HttpServletResponse response);
}
