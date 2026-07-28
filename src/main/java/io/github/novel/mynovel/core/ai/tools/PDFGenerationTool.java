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
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFGenerationTool {

    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile("!\\[([^]]*)]\\(([^)\\s]+)(?:\\s+\"[^\"]*\")?\\)");

    @Tool(description = "Generate a PDF file with given content. Supports plain text and common Markdown syntax, including images.")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF. Markdown image syntax like ![alt](path-or-url) will be rendered as images.") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String normalizedFileName = fileName.endsWith(".pdf") ? fileName : fileName + ".pdf";
        String filePath = fileDir + "/" + normalizedFileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 自定义字体（需要人工下载字体文件到特定目录）
                String fontPath = Paths.get("src/main/resources/static/fonts/微软雅黑.ttf")
                        .toAbsolutePath().toString();
                PdfFont font = PdfFontFactory.createFont(fontPath,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                // 使用内置中文字体
//                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);
                addMarkdownContent(document, content);
            }
            return "PDF generated successfully to: " + filePath;
        } catch (IOException e) {
            return "Error generating PDF: " + e.getMessage();
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

        Path projectRootPath = Paths.get(FileConstant.FILE_SAVE_DIR).getParent().resolve(imageLocation);
        if (Files.exists(projectRootPath)) {
            return projectRootPath.toAbsolutePath().toString();
        }

        Path downloadPath = Paths.get(FileConstant.FILE_SAVE_DIR, "download", imageLocation);
        if (Files.exists(downloadPath)) {
            return downloadPath.toAbsolutePath().toString();
        }

        return path.toAbsolutePath().toString();
    }
}
