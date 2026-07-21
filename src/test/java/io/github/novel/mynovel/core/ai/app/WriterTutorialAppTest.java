package io.github.novel.mynovel.core.ai.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
        Assertions.assertNotNull(content3);
    }
}