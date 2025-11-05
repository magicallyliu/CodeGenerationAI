package com.liuh.codegenerationbackend.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.exception.ThrowUtils;
import com.liuh.codegenerationbackend.manager.CosManager;
import com.liuh.codegenerationbackend.service.ScreenshotService;
import com.liuh.codegenerationbackend.utils.WebScreenshotUtils;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @Description 截图相关服务实现
 */

@SuppressWarnings("all")
@Slf4j
@RestController
@RequestMapping("/screenshot")
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private CosManager cosManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        //参数效验
        if (StrUtil.isBlank(webUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "截图网址不能为空");
        }
        log.info("开始截图: {}", webUrl);

        //本地截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "生成网页截图失败");

        //上传图片到cos 中
        String screenshotToCosPath;
        try {
            screenshotToCosPath = uploadScreenshotToCos(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(screenshotToCosPath), ErrorCode.OPERATION_ERROR, "上传图片到cos失败");
            log.info("截图成功, cos路径: {}", screenshotToCosPath);
            return screenshotToCosPath;
        } finally {
            //清理本地文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 上传图片到对象存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 返回的对象存储访问路径, 上传失败返回null
     */
    private String uploadScreenshotToCos(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        //判断截图是否存在
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在, 上传失败. {}", localScreenshotPath);
            return null;
        }

        //上传cos
        //配置保存的位置
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String cosKey = generateScreenshotKey(fileName);
        return cosManager.uploadFile(cosKey, screenshotFile);
    }

    /**
     * 生成cos存储路径
     * 格式 : /screenshot/yyyyMMdd/uuid.jpg
     *
     * @param fileName
     * @return
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshot/%s/%s", datePath, fileName);

    }

    /**
     * 清理本地文件
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            FileUtil.del(localFilePath);
            log.info("清理本地文件成功: {}", localFilePath);
        }
    }
}
