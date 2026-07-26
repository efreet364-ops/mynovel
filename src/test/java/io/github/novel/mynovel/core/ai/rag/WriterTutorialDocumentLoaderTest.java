package io.github.novel.mynovel.core.ai.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WriterTutorialDocumentLoaderTest {

    @Resource
    WriterTutorialDocumentLoader writerTutorialDocumentLoader;

    @Test
    void loadMarkdowns() {
        List<Document> documents = writerTutorialDocumentLoader.loadMarkdowns();
        Assertions.assertNotNull(documents);
    }
}