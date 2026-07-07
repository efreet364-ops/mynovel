package io.github.novel.mynovel;

import io.github.novel.mynovel.core.task.BookToEsTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BookToEsTaskTest {

    @Autowired
    private BookToEsTask bookToEsTask;

    @Test
    void runOnce() {
        bookToEsTask.syncBookToEs();
    }
}
