package io.github.novel.mynovel.core.ai.tools;

import io.github.novel.mynovel.core.util.AliyunOSSOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ToolRegistration {

    private final AliyunOSSOperator aliyunOSSOperator;

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Value("${search-api.url:https://www.searchapi.io/api/v1/search}")
    private String searchApiUrl;

    @Value("${app.file-save-dir:${java.io.tmpdir}/mynovel}")
    private String fileSaveDir;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool(fileSaveDir);
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey, searchApiUrl);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool(fileSaveDir);
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool(aliyunOSSOperator, fileSaveDir);
        TerminateTool terminateTool = new TerminateTool();
        return ToolCallbacks.from(
            fileOperationTool,
            webSearchTool,
            webScrapingTool,
            resourceDownloadTool,
            terminalOperationTool,
            pdfGenerationTool,
                terminateTool
        );
    }
}
