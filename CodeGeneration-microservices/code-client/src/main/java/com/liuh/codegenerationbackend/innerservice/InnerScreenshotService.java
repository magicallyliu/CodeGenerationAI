package com.liuh.codegenerationbackend.innerservice;

/**
 * @Description  内部截图服务
 */

@SuppressWarnings("all")

public interface InnerScreenshotService {

    /**
     * 截图 + 上传图片服务
     * 截取网页截图，上传到腾讯云cos
     * 返回访问地址
     *  @param webUrl 想要截图的网址
     * @return  访问地址
     */
    String generateAndUploadScreenshot(String webUrl);

}
