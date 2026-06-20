package io.github.novel.mynovel.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

public class CacheTest {

    @Test
    void testCache() {
        Cache<String, String> cache = Caffeine.newBuilder()
                .maximumSize(100)
                .recordStats()
                .build();
        // 存入缓存
        cache.put("name", "Tom");
        // 读取缓存
        String value = cache.getIfPresent("name");

        System.out.println(value);
    }
}
