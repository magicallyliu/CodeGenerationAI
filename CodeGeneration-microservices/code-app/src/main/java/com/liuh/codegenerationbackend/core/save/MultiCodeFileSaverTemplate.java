package com.liuh.codegenerationbackend.core.save;

import com.liuh.codegenerationbackend.ai.model.MultiFileCodeResult;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;

/**
 * @Description  模版模式 -- 多文件代码解析器
 */

@SuppressWarnings("all")

public class MultiCodeFileSaverTemplate extends  CodeFileSaveTemplate<MultiFileCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return  CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {

        saveToFile(baseDirPath,"index.html",result.getHtmlCode());
        saveToFile(baseDirPath,"script.js",result.getJsCode());
        saveToFile(baseDirPath,"style.css",result.getCssCode());
    }

}
