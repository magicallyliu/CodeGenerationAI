package com.liuh.codegenerationbackend.ai.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Description
 */

@SuppressWarnings("all")
@SpringBootTest
class FileWriteToolTest {

    @Test
    void write() {
        System.out.println(new FileWriteTool().write("src\\pages\\Blog.vue", "test.txt",11L));
    }
}