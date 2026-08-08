package io.github.novel.mynovel.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OssKeyUtilsTest {

    @Test
    void genAgentPdfKeyUsesAgentPrefixAndUserDirectory() {
        String key = OssKeyUtils.genAgentPdfKey(1001L, "report.pdf");

        assertTrue(key.startsWith("agent/pdf/1001/"));
        assertTrue(key.endsWith("-report.pdf"));
    }

    @Test
    void genAgentPdfKeySanitizesFileName() {
        String key = OssKeyUtils.genAgentPdfKey(1001L, "../bad\\name.txt");
        String fileName = key.substring(key.lastIndexOf('/') + 1);

        assertTrue(key.startsWith("agent/pdf/1001/"));
        assertTrue(key.endsWith(".pdf"));
        assertFalse(fileName.contains("/"));
        assertFalse(fileName.contains("\\"));
        assertFalse(fileName.contains(".."));
    }
}
