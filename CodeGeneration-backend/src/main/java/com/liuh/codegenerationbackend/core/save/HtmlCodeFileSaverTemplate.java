package com.liuh.codegenerationbackend.core.save;

import com.liuh.codegenerationbackend.ai.model.HtmlCodeResult;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;

/**
 * @Description 模版方法 -- 保存HTML文件
 */

@SuppressWarnings("all")

public class HtmlCodeFileSaverTemplate extends CodeFileSaveTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        saveToFile(baseDirPath, "index.html", result.getHtmlCode());
    }


}
