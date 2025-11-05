package com.liuh.codegenerationbackend.core.parser;

import com.liuh.codegenerationbackend.exception.BusinessException;
import com.liuh.codegenerationbackend.exception.ErrorCode;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;

/**
 * @Description  代码解析器执行器 --  策略模式
 */

@SuppressWarnings("all")

public class CodeParserExector {

    private static final HtmlCodeParser HTML_CODE_PARSER = new HtmlCodeParser();
    private static final MultiFileCodeParser MULTI_FILE_CODE_PARSER = new MultiFileCodeParser();
    /**
     *  执行代码解析
     * @param codeContent 代码的内容
     * @param codeGenTypeEnum 代码的生成类型
     * @return 解析结果(HTMLCodeResult | MultiFileCodeResult)
     */
    public static Object executeParser(String  codeContent, CodeGenTypeEnum  codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case CodeGenTypeEnum.HTML ->  HTML_CODE_PARSER.parseCode(codeContent);
            case CodeGenTypeEnum.MULTI_FILE ->  MULTI_FILE_CODE_PARSER.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR,  "不支持该类型的代码解析: " + codeGenTypeEnum.getValue());
        };
    }
}
