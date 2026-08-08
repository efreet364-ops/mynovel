package io.github.novel.mynovel.core.ai.tools;

import io.github.novel.mynovel.core.auth.UserHolder;
import io.github.novel.mynovel.core.util.AliyunOSSOperator;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() throws Exception {
        AliyunOSSOperator ossOperator = mockOssOperator("https://private-bucket.example.com/report.pdf?Expires=1");
        PDFGenerationTool tool = new PDFGenerationTool(ossOperator);
        UserHolder.setUserId(1001L);
        String fileName = "编程导航原创项目.pdf";
        String content = "编程导航原创项目 https://www.codefather.cn";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
        assertTrue(result.contains("[点击下载 编程导航原创项目.pdf]"));
        assertTrue(result.contains("https://private-bucket.example.com/report.pdf"));
        assertFalse(result.contains("/tmp/pdf/"));
        UserHolder.clear();
    }

    @Test
    public void testGeneratePDFWithMarkdownImage() throws Exception {
        AliyunOSSOperator ossOperator = mockOssOperator("https://private-bucket.example.com/markdown-image-test.pdf?Expires=1");
        PDFGenerationTool tool = new PDFGenerationTool(ossOperator);
        String fileName = "markdown-image-test.pdf";
        String content = """
                # 写作地点报告

                下面是一张图片：

                ![logo](tmp/download/logo.png)
                """;
        String result = tool.generatePDF(fileName, content);
        assertTrue(result.startsWith("PDF 已生成"));
        assertTrue(result.contains("链接 7 天内有效"));
    }

    private AliyunOSSOperator mockOssOperator(String downloadUrl) throws Exception {
        return new AliyunOSSOperator() {
            @Override
            public void uploadAgentPdf(Path filePath, String objectKey, String originalFilename) {
            }

            @Override
            public String generateAgentPdfDownloadUrl(String objectKey, String originalFilename) {
                return downloadUrl;
            }

            @Override
            public long getAgentUrlExpireDays() {
                return 7L;
            }

            @Override
            public String getAgentPrefix() {
                return "agent/pdf/";
            }
        };
    }
}
