package io.github.novel.mynovel.core.ai.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String api_key;

    @Value("${search-api.url}")
    private String apiUrl;

    @Test
    void searchWeb() {
        WebSearchTool tool = new WebSearchTool(api_key, apiUrl);
        String result = tool.searchWeb("哔哩哔哩bilibili");
        Assertions.assertNotNull(result);
    }
}
