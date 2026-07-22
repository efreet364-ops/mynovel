package io.github.novel.mynovel.core.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class ChatMemoryConfiguration {

    /**
     * 基于redis实现ai会话记忆持久化的配置类
     * @param redisTemplate
     * @return
     */
    @Bean
    public RedisChatMemoryRepositoryConfig redisChatMemoryRepositoryConfig(
            StringRedisTemplate redisTemplate
    ) {
        RedisChatMemoryRepositoryConfig config = new RedisChatMemoryRepositoryConfig();
        config.setRedisTemplate(redisTemplate);
        config.setKeyPrefix("writerTutorialChat:");
        config.setTimeToLive(7200);

        return config;
    }

}
