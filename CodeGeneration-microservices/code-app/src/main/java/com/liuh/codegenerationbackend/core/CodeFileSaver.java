package com.liuh.codegenerationbackend.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.liuh.codegenerationbackend.ai.model.HtmlCodeResult;
import com.liuh.codegenerationbackend.ai.model.MultiFileCodeResult;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @Description 将生成的代码保存(到本地服务器)
 */

@SuppressWarnings("all")
@Deprecated
public class CodeFileSaver {

    /**
     * \
     * 定义一个文件保存的根目录
     * System.getProperty("user.dir") 返回项目的绝对路径
     */
    private static final String FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output/";

    /**
     * 保存 HTML 文件代码
     *
     * @param htmlCodeResult HTML 代码结果
     *
     * @return 返回新的文件对象
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        //定义一个生成路径
        String fileUniquePath = buildFileUniquePath(CodeGenTypeEnum.HTML.getValue());
        //保存 HTML 文件
        saveFile(fileUniquePath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(fileUniquePath);
    }

    /**
     * 保存多文件代码
     * 文件有:
     * index.html
     * script.js
     * style.css
     *
     * @param multiFileCodeResult
     * @return 返回新的文件对象
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        //定义一个生成路径
        String fileUniquePath = buildFileUniquePath(CodeGenTypeEnum.MULTI_FILE.getValue());
        //保存 HTML 文件
        saveFile(fileUniquePath, "index.html", multiFileCodeResult.getHtmlCode());
        //保存 JS 文件
        saveFile(fileUniquePath, "script.js", multiFileCodeResult.getJsCode());
        //保存 CSS 文件
        saveFile(fileUniquePath, "style.css", multiFileCodeResult.getCssCode());
        return new File(fileUniquePath);
    }

    /**
     * 构建文件的唯一路径 (tmp/code_output/bizType_雪花ID)
     *
     * @param bizType 代码生成的类型(HTML/多文件)
     * @return
     */
    private static String buildFileUniquePath(String bizType) {
        String fileUniquePath = FILE_SAVE_ROOT_PATH + File.separator + bizType + "_" + IdUtil.getSnowflakeNextIdStr();
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
    private static void saveFile(String filePath, String fileName, String fileContent) {
        String fileAbsolutePath = filePath + File.separator + fileName;
        //将代码写入文件中
        FileUtil.writeString(fileContent, fileAbsolutePath, StandardCharsets.UTF_8);
    }
}
