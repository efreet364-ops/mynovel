package io.github.novel.mynovel.core.ai.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "编程导航原创项目.pdf";
        String content = "编程导航原创项目 https://www.codefather.cn";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }

    @Test
    public void testGeneratePDFWithMarkdownImage() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "markdown-image-test.pdf";
        String content = """
                # 写作地点报告

                下面是一张图片：

                ![logo](tmp/download/logo.png)
                """;
        String result = tool.generatePDF(fileName, content);
        assertTrue(result.startsWith("PDF generated successfully"));
    }
}
