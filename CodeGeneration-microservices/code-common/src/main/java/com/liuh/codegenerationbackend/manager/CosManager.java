package com.liuh.codegenerationbackend.manager;

import cn.hutool.core.util.ObjUtil;
import com.liuh.codegenerationbackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @Description cos对象存储管理
 */

@SuppressWarnings("all")
@Component
//只有配置了对象存储才会加载
@ConditionalOnBean(COSClient.class)
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 文件保存
     *
     * @param key  唯一键  保存的文件位置
     * @param file 文件
     * @return 上传到对象存储的结果
     */
    public PutObjectResult putObject(String key, File filePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, filePath);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件
     * 得到文件的返回结果, 并拼出可访问的路径
     *
     * @param key  唯一键  保存的文件位置
     * @param file 文件
     * @return 可访问的url地址, 失败返回null
     */
    public String uploadFile(String key, File file) {
        PutObjectResult putObjectResult = putObject(key, file);
        if (ObjUtil.isNotNull(putObjectResult)) {
            String url = String.format("%s/%s", cosClientConfig.getHost(), key);
            log.info("上传文件到对象存储成功, url: {} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("上传文件到对象存储失败, url: {} , 返回结果为空", file.getName());
            return null;
        }
    }
}
