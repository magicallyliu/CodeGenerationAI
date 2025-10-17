package com.liuh.codegenerationbackend.core.parser;

/**
 * @Description 代码解析器的策略接口
 */

@SuppressWarnings("all")

public interface CodeParser<T> {


    /**
     * 解析代码内容
     */
    T parseCode(String codeContent);
}
