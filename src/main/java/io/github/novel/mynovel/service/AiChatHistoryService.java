package io.github.novel.mynovel.service;

import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.dto.resp.AiChatMessageRespDto;
import io.github.novel.mynovel.dto.resp.AiChatSessionRespDto;
import java.util.List;

public interface AiChatHistoryService {

    void saveUserMessage(Long userId, String conversationId, String content);

    void saveAssistantMessage(Long userId, String conversationId, String content);

    RestResp<List<AiChatSessionRespDto>> listSessions(Long userId);

    RestResp<List<AiChatMessageRespDto>> listMessages(Long userId, String conversationId);

    RestResp<Void> deleteSession(Long userId, String conversationId);
}
