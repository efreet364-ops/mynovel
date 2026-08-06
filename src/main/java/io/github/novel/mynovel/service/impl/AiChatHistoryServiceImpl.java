package io.github.novel.mynovel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.novel.mynovel.core.common.constant.ErrorCodeEnum;
import io.github.novel.mynovel.core.common.exception.BusinessException;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dao.entity.AiChatMessage;
import io.github.novel.mynovel.dao.entity.AiChatSession;
import io.github.novel.mynovel.dao.mapper.AiChatMessageMapper;
import io.github.novel.mynovel.dao.mapper.AiChatSessionMapper;
import io.github.novel.mynovel.dto.resp.AiChatMessageRespDto;
import io.github.novel.mynovel.dto.resp.AiChatSessionRespDto;
import io.github.novel.mynovel.service.AiChatHistoryService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiChatHistoryServiceImpl implements AiChatHistoryService {

    private static final int TITLE_MAX_LENGTH = 40;

    private static final int LAST_MESSAGE_MAX_LENGTH = 120;

    private final AiChatSessionMapper aiChatSessionMapper;

    private final AiChatMessageMapper aiChatMessageMapper;

    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserMessage(Long userId, String conversationId, String content) {
        AiChatSession session = getOrCreateSession(userId, conversationId, content);
        saveMessage(session, "user", content);
        updateSessionAfterMessage(session, content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAssistantMessage(Long userId, String conversationId, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        AiChatSession session = requireOwnedSession(userId, conversationId);
        saveMessage(session, "assistant", content);
        updateSessionAfterMessage(session, content);
    }

    @Override
    public RestResp<List<AiChatSessionRespDto>> listSessions(Long userId) {
        QueryWrapper<AiChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("update_time");
        List<AiChatSessionRespDto> sessions = aiChatSessionMapper.selectList(queryWrapper)
                .stream()
                .map(this::toSessionResp)
                .toList();
        return RestResp.ok(sessions);
    }

    @Override
    public RestResp<List<AiChatMessageRespDto>> listMessages(Long userId, String conversationId) {
        AiChatSession session = requireOwnedSession(userId, conversationId);
        QueryWrapper<AiChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", session.getId())
                .eq("user_id", userId)
                .orderByAsc("id");
        List<AiChatMessageRespDto> messages = aiChatMessageMapper.selectList(queryWrapper)
                .stream()
                .map(this::toMessageResp)
                .toList();
        return RestResp.ok(messages);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RestResp<Void> deleteSession(Long userId, String conversationId) {
        AiChatSession session = requireOwnedSession(userId, conversationId);

        QueryWrapper<AiChatMessage> messageQuery = new QueryWrapper<>();
        messageQuery.eq("session_id", session.getId())
                .eq("user_id", userId);
        aiChatMessageMapper.delete(messageQuery);
        aiChatSessionMapper.deleteById(session.getId());
        jdbcChatMemoryRepository.deleteByConversationId(conversationId);

        return RestResp.ok();
    }

    private AiChatSession getOrCreateSession(Long userId, String conversationId, String firstMessage) {
        Assert.notNull(userId, "userId cannot be null");
        Assert.hasText(conversationId, "conversationId cannot be empty");

        AiChatSession session = findByConversationId(conversationId);
        if (session != null) {
            assertOwner(session, userId);
            return session;
        }

        LocalDateTime now = LocalDateTime.now();
        AiChatSession newSession = new AiChatSession();
        newSession.setUserId(userId);
        newSession.setConversationId(conversationId);
        newSession.setTitle(ellipsis(firstMessage, TITLE_MAX_LENGTH));
        newSession.setLastMessage("");
        newSession.setMessageCount(0);
        newSession.setCreateTime(now);
        newSession.setUpdateTime(now);
        aiChatSessionMapper.insert(newSession);
        return newSession;
    }

    private AiChatSession requireOwnedSession(Long userId, String conversationId) {
        Assert.notNull(userId, "userId cannot be null");
        Assert.hasText(conversationId, "conversationId cannot be empty");

        AiChatSession session = findByConversationId(conversationId);
        if (session == null) {
            throw new BusinessException(ErrorCodeEnum.USER_REQUEST_PARAM_ERROR);
        }
        assertOwner(session, userId);
        return session;
    }

    private AiChatSession findByConversationId(String conversationId) {
        QueryWrapper<AiChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId)
                .last("limit 1");
        return aiChatSessionMapper.selectOne(queryWrapper);
    }

    private void assertOwner(AiChatSession session, Long userId) {
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCodeEnum.USER_UN_AUTH);
        }
    }

    private void saveMessage(AiChatSession session, String role, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(session.getId());
        message.setUserId(session.getUserId());
        message.setConversationId(session.getConversationId());
        message.setRole(role);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        aiChatMessageMapper.insert(message);
    }

    private void updateSessionAfterMessage(AiChatSession session, String content) {
        AiChatSession update = new AiChatSession();
        update.setId(session.getId());
        update.setLastMessage(ellipsis(content, LAST_MESSAGE_MAX_LENGTH));
        update.setMessageCount((session.getMessageCount() == null ? 0 : session.getMessageCount()) + 1);
        update.setUpdateTime(LocalDateTime.now());
        aiChatSessionMapper.updateById(update);
        session.setMessageCount(update.getMessageCount());
    }

    private AiChatSessionRespDto toSessionResp(AiChatSession session) {
        return AiChatSessionRespDto.builder()
                .conversationId(session.getConversationId())
                .title(session.getTitle())
                .lastMessage(session.getLastMessage())
                .messageCount(session.getMessageCount())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .build();
    }

    private AiChatMessageRespDto toMessageResp(AiChatMessage message) {
        return AiChatMessageRespDto.builder()
                .id(message.getId())
                .role(message.getRole())
                .content(message.getContent())
                .createTime(message.getCreateTime())
                .build();
    }

    private String ellipsis(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "新的对话";
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength - 1) + "...";
    }
}
