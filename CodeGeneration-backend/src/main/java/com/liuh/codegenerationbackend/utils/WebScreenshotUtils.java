package com.liuh.codegenerationbackend.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

/**
 * @Description 使用selenium进行自动网页截图
 * 初始化驱动 需要避免重复初始化驱动
 */

@SuppressWarnings("all")
@Slf4j
public class WebScreenshotUtils {

    //1.

    //1.2 默认使用已经准备好的驱动化实例


    private static final WebDriver WEB_DRIVER;

    //1.1 在静态代码块中初始化驱动, 确保整个应用生命周期只能初始化一次
    static {
        //默认宽高
        final int DEFAULT_WIDTH = 1600;
        final int DEFAULT_HEIGHT = 900;
        //初始化驱动
        WEB_DRIVER = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        System.setProperty("wdm.timeout", "300");
        System.setProperty("wdm.retryCount", "3");
// 设置国内镜像
        System.setProperty("wdm.chromeDownloadUrl", "https://npmmirror.com/mirrors/chromedriver/");

    }


    /**
     * 获取网页的截图
     *
     * @param webUrl 网页地址
     * @return 压缩后的截图路径, 失败的话返回null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        //非空效验
        if (ObjUtil.isNull(webUrl)) {
            log.error("网页地址不能为空");
            return null;
        }

        try {
            // 创建临时目录,
            String rootPath = System.getProperty("user.dir") + "/tmp/screenshots/" + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);
            //图片后缀
            final String IMAGE_SUFFIX = ".png";
            //保存原始图片的文件路径
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

            //访问网页
            WEB_DRIVER.get(webUrl);

            //等待网页加载
            waitForPageLoad(WEB_DRIVER);

            //截取网页
            byte[] screenshotAs = ((TakesScreenshot) WEB_DRIVER).getScreenshotAs(OutputType.BYTES);
            //保存原始图片
            saveImage(screenshotAs, imageSavePath);
            log.info("保存原始图片: {}", imageSavePath);

            //压缩图片
            //压缩图片的路径
            final String COMPRESSED_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSED_SUFFIX;
            compressImage(imageSavePath, compressedImagePath);
            log.info("压缩图片: {}", compressedImagePath);

            //删除原始图片
            FileUtil.del(imageSavePath);
            log.info("删除原始图片: {}", imageSavePath);

            //返回压缩后的图片路径
            return compressedImagePath;
        } catch (Exception e) {
            log.error("获取网页截图失败: {}", webUrl, e);
            return null;
        }
    }

    /**
     * 在应用结束前, 关闭服务器
     * 在项目使用停止前销毁驱动, 释放资源
     */
    @PreDestroy
    public void destroy() {
        //关闭驱动
        WEB_DRIVER.quit();
    }

    /**
     * 保存截取的网页图片
     *
     * @param imageBytes
     * @param imagePath
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 压缩图片
     *
     * @param originImagePath     原本的图像路径
     * @param compressedImagePath 压缩后的图像路径
     */
    private static void compressImage(String originImagePath, String compressedImagePath) {
        //压缩图片的质量
        final float COMPRESSION_QUALITY = 0.3f;
        //使用hutool工具类进行图片压缩
        try {
            ImgUtil.compress(
                    FileUtil.file(originImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY);
        } catch (IORuntimeException e) {
            log.error("压缩图片失败: {} --> {}", originImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 等待页面加载完成
     */
    private static void waitForPageLoad(WebDriver webDriver) {

        try {
            //等待的超时时间
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            // 等待 document.readyState 为complete
            wait.until(driver ->
                    ((JavascriptExecutor) driver).executeScript("return document.readyState")
                            .equals("complete")
            );
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }

    }


    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
//            System.setProperty("wdm.chromeDriverMirrorUrl", "https://registry.npmmirror.com/binary.html?path=chromedriver");
            WebDriverManager.chromedriver().useMirror().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();

            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动

            WebDriver driver = new ChromeDriver(options);

            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }


}