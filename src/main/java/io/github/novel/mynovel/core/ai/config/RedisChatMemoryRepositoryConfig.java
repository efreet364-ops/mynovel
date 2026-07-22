package io.github.novel.mynovel.core.ai.config;

import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisChatMemoryRepositoryConfig {

    private StringRedisTemplate redisTemplate;

    /**
     * Redis Key 前缀
     */
    private String keyPrefix;

    /**
     * 过期时间（秒）
     */
    private long timeToLive;

    public StringRedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }
}
