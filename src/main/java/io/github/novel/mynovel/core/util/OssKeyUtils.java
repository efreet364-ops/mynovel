package io.github.novel.mynovel.core.util;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/*
* 生成上传文件的OSS存储Key
*
*  */
public class OssKeyUtils {

    private static final String DEFAULT_AGENT_PDF_PREFIX = "agent/pdf/";

    public static String genSimpleKey(String originalFilename) {
        // 填写Object完整路径，例如202406/1.png。Object完整路径中不能包含Bucket名称。
        //获取当前系统日期的字符串,格式为 yyyy/MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        //生成一个新的不重复的文件名
        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        return objectName;
    }

    public static String genAgentPdfKey(Long userId, String originalFilename) {
        return genAgentPdfKey(DEFAULT_AGENT_PDF_PREFIX, userId, originalFilename);
    }

    public static String genAgentPdfKey(String prefix, Long userId, String originalFilename) {
        String normalizedPrefix = normalizePrefix(prefix);
        String owner = userId == null ? "anonymous" : String.valueOf(userId);
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String safeFileName = sanitizePdfFileName(originalFilename);
        return normalizedPrefix + owner + "/" + dir + "/" + UUID.randomUUID() + "-" + safeFileName;
    }

    public static String sanitizePdfFileName(String originalFilename) {
        String fileName = originalFilename == null ? "" : originalFilename.trim();
        fileName = fileName.replaceAll("[\\\\/]+", "_")
                .replaceAll("[\\p{Cntrl}]", "")
                .replace("..", "_");
        if (fileName.isBlank()) {
            fileName = "agent-report.pdf";
        }
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0) {
                fileName = fileName.substring(0, lastDot);
            }
            fileName = fileName + ".pdf";
        }
        return fileName;
    }

    private static String normalizePrefix(String prefix) {
        String value = prefix == null || prefix.isBlank() ? DEFAULT_AGENT_PDF_PREFIX : prefix.trim();
        value = value.replaceAll("^/+", "");
        return value.endsWith("/") ? value : value + "/";
    }
}
