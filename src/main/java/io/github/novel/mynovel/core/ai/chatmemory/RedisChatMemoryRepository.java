package io.github.novel.mynovel.core.ai.chatmemory;

import com.alibaba.fastjson2.JSON;
import io.github.novel.mynovel.core.ai.config.RedisChatMemoryRepositoryConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    @Resource
    private RedisChatMemoryRepositoryConfig redisChatMemoryRepositoryConfig;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 通过 ScanOptions 扫描所有匹配特定前缀的键，避免使用 KEYS 命令可能导致的阻塞风险。
     * 在遍历扫描结果时，对键进行分割处理，提取出会话 ID，最终返回包含所有会话 ID 的列表。
     * @return
     */
    @Override
    public @Nullable List<String> findConversationIds() {
        return redisChatMemoryRepositoryConfig.getRedisTemplate()
                .execute((RedisCallback<List<String>>) connection -> {
                    var keys = new HashSet<String>();
                    ScanOptions options =
                            ScanOptions.scanOptions()
                                    .match(String.format("*%s*", redisChatMemoryRepositoryConfig.getKeyPrefix()))
                                    .count(Integer.MAX_VALUE)
                                    .build();

                    try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                        while (cursor.hasNext()) {
                            String[] key = new String(cursor.next(), StandardCharsets.UTF_8).split(":");
                            if (key.length > 0) {
                                keys.add(key[key.length - 1]);
                            }
                        }
                    }
                    return new ArrayList<>(keys);
                });
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        Assert.hasText(conversationId, "会话id不可为空");

        String key = redisChatMemoryRepositoryConfig.getKeyPrefix() + conversationId;
        List<String> messageStrings = redisChatMemoryRepositoryConfig.getRedisTemplate()
                .opsForList().range(key, 0, -1);
        if (messageStrings == null) {
            log.debug("No messages found for conversationId: " + conversationId);
            return List.of();
        }

        List<Message> messages = new ArrayList<>();
        for (String messageString : messageStrings) {
            try {
                messages.add(toMessage(JSON.parseObject(messageString, StoredMessage.class)));
            } catch (RuntimeException e) {
                throw new RuntimeException("Error deserializing message", e);
            }
        }
        return messages;
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.hasText(conversationId, "会话id不可为空");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");

        String key = redisChatMemoryRepositoryConfig.getKeyPrefix() + conversationId;

        deleteByConversationId(conversationId);

        List<String> messageJsons = messages.stream()
                .map(message -> {
                    try {
                        message.getMetadata().put("timestamp", Instant.now().toString());
                        return JSON.toJSONString(StoredMessage.from(message));
                    } catch (RuntimeException e) {
                        throw new RuntimeException("Error serializing message", e);
                    }
                })
                .toList();

        redisChatMemoryRepositoryConfig.getRedisTemplate().opsForList().rightPushAll(key, messageJsons);
        if (redisChatMemoryRepositoryConfig.getTimeToLive() > 0) {
            redissonClient.getKeys()
                    .expire(key, redisChatMemoryRepositoryConfig.getTimeToLive(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        Assert.hasText(conversationId, "会话id不可为空");

        String key = redisChatMemoryRepositoryConfig.getKeyPrefix() + conversationId;
        redisChatMemoryRepositoryConfig.getRedisTemplate().delete(key);
    }

    private Message toMessage(StoredMessage storedMessage) {
        Map<String, Object> metadata = storedMessage.metadata == null
                ? Map.of()
                : new HashMap<>(storedMessage.metadata);
        MessageType messageType = MessageType.valueOf(storedMessage.messageType);

        return switch (messageType) {
            case USER -> UserMessage.builder()
                    .text(storedMessage.text)
                    .metadata(metadata)
                    .build();
            case ASSISTANT -> new AssistantMessage(
                    storedMessage.text,
                    metadata,
                    storedMessage.toolCalls == null ? List.of() : storedMessage.toolCalls);
            case SYSTEM -> SystemMessage.builder()
                    .text(storedMessage.text)
                    .metadata(metadata)
                    .build();
            case TOOL -> new ToolResponseMessage(
                    storedMessage.toolResponses == null ? List.of() : storedMessage.toolResponses,
                    metadata);
        };
    }

    private static class StoredMessage {

        public String messageType;

        public String text;

        public Map<String, Object> metadata;

        public List<AssistantMessage.ToolCall> toolCalls;

        public List<ToolResponseMessage.ToolResponse> toolResponses;

        public static StoredMessage from(Message message) {
            StoredMessage storedMessage = new StoredMessage();
            storedMessage.messageType = message.getMessageType().name();
            storedMessage.text = message.getText();
            storedMessage.metadata = new HashMap<>(message.getMetadata());

            if (message instanceof AssistantMessage assistantMessage) {
                storedMessage.toolCalls = assistantMessage.getToolCalls();
            } else if (message instanceof ToolResponseMessage toolResponseMessage) {
                storedMessage.toolResponses = toolResponseMessage.getResponses();
            }

            return storedMessage;
        }
    }
}
