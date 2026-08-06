package io.github.novel.mynovel.core.ai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.MysqlChatMemoryRepositoryDialect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ChatMemoryConfiguration {

    /**
     * 基于redis实现ai会话记忆持久化的配置类
     * @param redisTemplate
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "ai.chat-memory.repository", havingValue = "redis")
    public RedisChatMemoryRepositoryConfig redisChatMemoryRepositoryConfig(
            StringRedisTemplate redisTemplate
    ) {
        RedisChatMemoryRepositoryConfig config = new RedisChatMemoryRepositoryConfig();
        config.setRedisTemplate(redisTemplate);
        config.setKeyPrefix("writerTutorialChat:");
        config.setTimeToLive(7200);

        return config;
    }

    /**
     * 基于 Spring AI 官方 JDBC 实现的 MySQL 会话记忆持久化。
     */
    @Bean
    public JdbcChatMemoryRepository jdbcChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new MysqlChatMemoryRepositoryDialect())
                .build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .build();
    }

}
