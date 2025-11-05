package com.liuh.codegenerationbackend.service.impl;

import com.liuh.codegenerationbackend.innerservice.InnerScreenshotService;
import com.liuh.codegenerationbackend.service.ScreenshotService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @Description 内部服务实现类 -- 截图服务
 */

@SuppressWarnings("all")
@DubboService
public class InnerScreenshotServiceImpl implements InnerScreenshotService {

    @Resource
    private ScreenshotService screenshotService;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        return screenshotService.generateAndUploadScreenshot(webUrl);
    }
}
