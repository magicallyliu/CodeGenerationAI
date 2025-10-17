package com.liuh.codegenerationbackend.core.save;

import com.liuh.codegenerationbackend.ai.model.HtmlCodeResult;
import com.liuh.codegenerationbackend.ai.model.MultiFileCodeResult;
import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * @Description 模版方法  -- 代码文件保存执行器
 * 根据不同的代码类型，调用不同的代码保存器
 */

@SuppressWarnings("all")

public class CodeFileSaveExector {
    private static final HtmlCodeFileSaverTemplate HTML_CODE_FILE_SAVER_TEMPLATE = new HtmlCodeFileSaverTemplate();
    private static final MultiCodeFileSaverTemplate MULTI_FILE_CODE_FILE_SAVER_TEMPLATE = new MultiCodeFileSaverTemplate();

    /**
     * 执行代码保存
     *
     * @param codeResult      代码生成结果
     * @param codeGenTypeEnum 代码生成类型
     * @return 保存代码的目录对象 --  保存代码的位置
     */
    public static File executeSave(Object codeResult, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> HTML_CODE_FILE_SAVER_TEMPLATE.saveCode((HtmlCodeResult) codeResult);
            case MULTI_FILE -> MULTI_FILE_CODE_FILE_SAVER_TEMPLATE.saveCode((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,  "不支持该类型的代码保存: " + codeGenTypeEnum.getValue());
        };
    }

}
