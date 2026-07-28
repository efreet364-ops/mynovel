package io.github.novel.mynovel.core.ai.agent;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@Slf4j
@SpringBootTest
class MyManusTest {

    @Resource
    private MyManus manus;

    @Test
    void run() {
        String userPrompt = """
                我想在常州市钟楼区找一个适合写作的地方，请帮我找一个适合写作的地点。
                并结合几张网络图片定制一份简单的写作地点分析报告，
                并以pdf格式输出。
                """;
        String answer = manus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}