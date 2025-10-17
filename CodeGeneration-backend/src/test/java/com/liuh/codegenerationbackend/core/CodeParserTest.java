package com.liuh.codegenerationbackend.core;


import com.liuh.codegenerationbackend.ai.model.HtmlCodeResult;
import com.liuh.codegenerationbackend.ai.model.MultiFileCodeResult;
import com.liuh.codegenerationbackend.core.parser.CodeParserExector;
import com.liuh.codegenerationbackend.model.enums.CodeGenTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * @Description
 */

@SuppressWarnings("all")
@SpringBootTest
class CodeParserTest {

    @Test
    void parseHtmlCode() {
        String codeContent = """
                随便写一段描述：
                html 格式
                <!DOCTYPE html>
                <html>
                <head>
                    <title>测试页面</title>
                </head>
                <body>
                    <h1>Hello World!</h1>
                </body>
                </html>

                随便写一段描述
                """;
        HtmlCodeResult result = (HtmlCodeResult) CodeParserExector.executeParser(codeContent, CodeGenTypeEnum.HTML);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
    }

    @Test
    void parseMultiFileCode() {
        String codeContent = """
                创建一个完整的网页：
                html 格式
                <!DOCTYPE html>
                <html>
                <head>
                    <title>多文件示例</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                    <h1>欢迎使用</h1>
                    <script src="script.js"></script>
                </body>
                </html>

                css 格式
                h1 {
                    color: blue;
                    text-align: center;
                }
                ```
                ```js
                console.log('页面加载完成');

                文件创建完成！
                """;
        MultiFileCodeResult result = (MultiFileCodeResult) CodeParserExector.executeParser(codeContent, CodeGenTypeEnum.MULTI_FILE);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        assertNotNull(result.getCssCode());
        assertNotNull(result.getJsCode());
    }
}
