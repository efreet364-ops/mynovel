package io.github.novel.mynovel.core.ai.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import io.github.novel.mynovel.core.ai.constant.FileConstant;
import io.github.novel.mynovel.core.auth.UserHolder;
import io.github.novel.mynovel.core.util.AliyunOSSOperator;
import io.github.novel.mynovel.core.util.OssKeyUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFGenerationTool {

    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[([^]]*)]\\(([^)\\s]+)(?:\\s+\"[^\"]*\")?\\)");

    private final AliyunOSSOperator aliyunOSSOperator;

    private final String fileSaveDir;

    public PDFGenerationTool(AliyunOSSOperator aliyunOSSOperator) {
        this(aliyunOSSOperator, FileConstant.DEFAULT_FILE_SAVE_DIR);
    }

    public PDFGenerationTool(AliyunOSSOperator aliyunOSSOperator, String fileSaveDir) {
        this.aliyunOSSOperator = aliyunOSSOperator;
        this.fileSaveDir = fileSaveDir;
    }

    @Tool(description = "Generate a PDF file with given content. Supports plain text and common Markdown syntax, including images.")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF. Markdown image syntax like ![alt](path-or-url) will be rendered as images.") String content) {
        String fileDir = fileSaveDir + "/pdf";
        String normalizedFileName = OssKeyUtils.sanitizePdfFileName(fileName);
        Path pdfPath = Paths.get(fileDir, normalizedFileName);
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(pdfPath.toString());
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                PdfFont font = loadFont();
                document.setFont(font);
                addMarkdownContent(document, content);
            }
            String objectKey = OssKeyUtils.genAgentPdfKey(
                    aliyunOSSOperator.getAgentPrefix(),
                    UserHolder.getUserId(),
                    normalizedFileName);
            aliyunOSSOperator.uploadAgentPdf(pdfPath, objectKey, normalizedFileName);
            String downloadUrl = aliyunOSSOperator.generateAgentPdfDownloadUrl(objectKey, normalizedFileName);
            deleteLocalPdf(pdfPath);
            return """
                    PDF 已生成：[点击下载 %s](%s)

                    链接 %d 天内有效。
                    """.formatted(
                    escapeMarkdownLinkText(normalizedFileName),
                    downloadUrl,
                    aliyunOSSOperator.getAgentUrlExpireDays()
            );
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
        } catch (Exception e) {
            return "PDF 已生成，但上传到 OSS 或生成下载链接失败：" + e.getMessage();
        }
    }

    private void addMarkdownContent(Document document, String content) throws IOException {
        for (String rawLine : content.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                document.add(new Paragraph(" "));
                continue;
            }

            Matcher imageMatcher = MARKDOWN_IMAGE_PATTERN.matcher(line);
            if (imageMatcher.matches()) {
                addImage(document, imageMatcher.group(2), imageMatcher.group(1));
                continue;
            }

            document.add(createParagraph(line));
        }
    }

    private Paragraph createParagraph(String line) {
        int headingLevel = headingLevel(line);
        if (headingLevel > 0) {
            String title = line.substring(headingLevel).trim();
            return new Paragraph(title)
                    .setFontSize(Math.max(12, 22 - headingLevel * 2))
                    .setMarginTop(headingLevel == 1 ? 14 : 8)
                    .setMarginBottom(6);
        }

        String normalized = line
                .replaceAll("^[-*+]\\s+", "• ")
                .replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
                .replaceAll("__([^_]+)__", "$1")
                .replaceAll("`([^`]+)`", "$1");
        return new Paragraph(normalized).setFontSize(11).setMarginBottom(4);
    }

    private int headingLevel(String line) {
        int level = 0;
        while (level < line.length() && line.charAt(level) == '#') {
            level++;
        }
        return level > 0 && level <= 6 && level < line.length() && line.charAt(level) == ' ' ? level : 0;
    }

    private void addImage(Document document, String imageLocation, String altText) {
        try {
            String resolvedLocation = resolveImageLocation(imageLocation);
            ImageData imageData = ImageDataFactory.create(resolvedLocation);
            Image image = new Image(imageData);
            image.setAutoScale(true);
            image.setMarginTop(8);
            image.setMarginBottom(8);
            document.add(image);

            if (altText != null && !altText.isBlank()) {
                document.add(new Paragraph(altText)
                        .setFontSize(9)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(0)
                        .setMarginBottom(8));
            }
        } catch (Exception e) {
            document.add(new Paragraph("[图片加载失败: " + imageLocation + "]")
                    .setFontSize(9)
                    .setMarginBottom(8));
        }
    }

    private String resolveImageLocation(String imageLocation) {
        if (imageLocation.startsWith("http://") || imageLocation.startsWith("https://")) {
            return imageLocation;
        }

        String pathText = imageLocation.startsWith("file:")
                ? Paths.get(URI.create(imageLocation)).toString()
                : imageLocation;
        Path path = Paths.get(pathText);
        if (Files.exists(path)) {
            return path.toAbsolutePath().toString();
        }

        Path projectRootPath = Paths.get(fileSaveDir).getParent().resolve(imageLocation);
        if (Files.exists(projectRootPath)) {
            return projectRootPath.toAbsolutePath().toString();
        }

        Path downloadPath = Paths.get(fileSaveDir, "download", imageLocation);
        if (Files.exists(downloadPath)) {
            return downloadPath.toAbsolutePath().toString();
        }

        return path.toAbsolutePath().toString();
    }

    private PdfFont loadFont() throws IOException {
        String fontResourcePath = "static/fonts/微软雅黑.ttf";
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fontResourcePath)) {
            if (inputStream == null) {
                return PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
            }
            byte[] fontBytes = inputStream.readAllBytes();
            return PdfFontFactory.createFont(fontBytes, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        }
    }

    private void deleteLocalPdf(Path pdfPath) {
        try {
            Files.deleteIfExists(pdfPath);
        } catch (IOException ignored) {
        }
    }

    private String escapeMarkdownLinkText(String text) {
        return text.replace("[", "\\[").replace("]", "\\]");
    }
}
