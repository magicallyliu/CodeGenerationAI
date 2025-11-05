package com.liuh.codegenerationbackend.service;



/**
 * @Description 截图相关操作
 */

@SuppressWarnings("all")
public interface ScreenshotService {

    /**
     * 截图 + 上传图片服务
     * 截取网页截图，上传到腾讯云cos
     * 返回访问地址
     *  @param webUrl 想要截图的网址
     * @return  访问地址
     */
    String generateAndUploadScreenshot(String webUrl);
}
