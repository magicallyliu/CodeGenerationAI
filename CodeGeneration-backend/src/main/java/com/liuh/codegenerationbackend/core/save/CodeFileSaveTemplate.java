package com.liuh.codegenerationbackend.core.save;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @Description 代码保存器(抽象) -- 模版方法模式
 */

@SuppressWarnings("all")

public abstract class CodeFileSaveTemplate<T> {

    /**
     * \
     * 定义一个文件保存的根目录
     * System.getProperty("user.dir") 返回项目的绝对路径
     */
    private static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output/";

    /**
     * 定义保存代码文件的流程
     *
     * @param result 代码生成结果
     * @return 返回文件目录对象 -- 保存代码的位置
     */
    public final File saveCode(T result) {
        //1. 验证输入
        validateInput(result);
        //2. 构建唯一目录
        String baseDirPath = buildFileUniquePath();
        //3.  保存代码文件
        saveFiles(result, baseDirPath);
        //4. 返回文件目录对象
        return new File(baseDirPath);
    }


    /**
     * 验证输入参数, 可以由子类重写
     *
     * @param result
     */
    protected void validateInput(T result) {
        // 验证输入 , 不能为空
        if (ObjUtil.isEmpty(result)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码生成结果不能为空");
        }

    }


    /**
     * 获取代码生成的类型
     *
     * @return
     */
    protected abstract CodeGenTypeEnum getCodeType();

    /**
     * 保存代码文件
     *
     * @param result      代码生成结果
     * @param baseDirPath 代码保存的目录
     */
    protected abstract void saveFiles(T result, String baseDirPath);


    /**
     * 构建文件的唯一路径 (tmp/code_output/codeType_雪花ID)
     *
     * @param bizType 代码生成的类型(HTML/多文件)
     * @return 返回文件的唯一路径
     */
    protected final String buildFileUniquePath() {
        String codeType = getCodeType().getValue();
        String fileUniquePath = FILE_SAVE_ROOT_PATH + File.separator + codeType + "_" + IdUtil.getSnowflakeNextIdStr();
        //创建路径
        FileUtil.mkdir(fileUniquePath);
        return fileUniquePath;
    }


    /**
     * 保存单个文件(通用方法)
     *
     * @param filePath    保存文件的路径
     * @param fileName    保存文件的名称
     * @param fileContent 保存文件的代码内容
     */
    protected final void saveToFile(String filePath, String fileName, String fileContent) {
        //代码不为空, 才去写入
        if (StrUtil.isNotBlank(fileContent)) {
            String fileAbsolutePath = filePath + File.separator + fileName;
            //将代码写入文件中
            FileUtil.writeString(fileContent, fileAbsolutePath, StandardCharsets.UTF_8);
        }

    }
}
