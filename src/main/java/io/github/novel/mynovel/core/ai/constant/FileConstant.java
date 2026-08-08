package io.github.novel.mynovel.core.ai.constant;

import java.nio.file.Paths;

public final class FileConstant {

    private FileConstant() {
    }

    /**
     * AI 工具本地临时文件默认保存目录。
     */
    public static final String DEFAULT_FILE_SAVE_DIR = Paths.get(
            System.getProperty("java.io.tmpdir"), "mynovel").toString();
}
