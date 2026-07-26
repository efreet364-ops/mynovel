package io.github.novel.mynovel.core.ai.app;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ai.rag.Query;

import java.util.List;
import java.util.UUID;



@SpringBootTest
class WriterTutorialAppTest {

    @Resource
    private  WriterTutorialApp writerTutorialApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        String content1 = writerTutorialApp.doChat("你好，我是efreet", chatId);

        String content2 = writerTutorialApp.doChat("你会写小说吗", chatId);

        String content3 = writerTutorialApp.doChat("我是谁", chatId);
        Assertions.assertNotNull(content1);
    }

    @Test
    void retriever() {
        var dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .build();
        DocumentRetriever retriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName("writerTutorial")
                        .build());

        List<Document> documentList = retriever.retrieve(new Query("我是一名新手作家，我想知道如何写作保持身体健康。"));

        Assertions.assertNotNull(documentList);
    }

    @Test
    void doChatWithRag() {
        String content = writerTutorialApp.doChatWithRag("我是一名新手作家，我想知道如何写作保持身体健康。",
                UUID.randomUUID().toString());

        Assertions.assertNotNull(content);

    }
}