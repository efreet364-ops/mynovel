package io.github.novel.mynovel.controller.front;

import io.github.novel.mynovel.core.auth.UserHolder;
import io.github.novel.mynovel.core.common.resp.RestResp;
import io.github.novel.mynovel.core.ai.agent.MyManus;
import io.github.novel.mynovel.core.ai.app.WriterTutorialApp;
import io.github.novel.mynovel.core.constant.ApiRouterConsts;
import io.github.novel.mynovel.dto.resp.AiChatMessageRespDto;
import io.github.novel.mynovel.dto.resp.AiChatSessionRespDto;
import io.github.novel.mynovel.service.AiChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_AI_URL_PREFIX)
public class AiController {

    @Resource
    private WriterTutorialApp writerTutorialApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private AiChatHistoryService aiChatHistoryService;

    /**
     * 普通聊天
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/writer_tutorial/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithWriterTutorialSse(String message, String chatId) {
        Long userId = UserHolder.getUserId();
        aiChatHistoryService.saveUserMessage(userId, chatId, message);

        SseEmitter sseEmitter = new SseEmitter(180000L);
        StringBuilder answer = new StringBuilder();
        writerTutorialApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        answer.append(chunk);
                        sseEmitter.send(SseEmitter.event().name("message").data(chunk));
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, () -> {
                    aiChatHistoryService.saveAssistantMessage(userId, chatId, answer.toString());
                    sseEmitter.complete();
                });

        return sseEmitter;
    }

    /**
     * RAG聊天
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/writer_tutorial/chatRag/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithRagWriterTutorialSse(String message, String chatId) {
        Long userId = UserHolder.getUserId();
        aiChatHistoryService.saveUserMessage(userId, chatId, message);

        SseEmitter sseEmitter = new SseEmitter(180000L);
        StringBuilder answer = new StringBuilder();
        writerTutorialApp.doChatWithRagByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        answer.append(chunk);
                        sseEmitter.send(SseEmitter.event().name("message").data(chunk));
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, () -> {
                    aiChatHistoryService.saveAssistantMessage(userId, chatId, answer.toString());
                    sseEmitter.complete();
                });

        return sseEmitter;
    }

    @GetMapping("/writer_tutorial/sessions")
    public RestResp<List<AiChatSessionRespDto>> listWriterTutorialSessions() {
        return aiChatHistoryService.listSessions(UserHolder.getUserId());
    }

    @GetMapping("/writer_tutorial/sessions/{conversationId}/messages")
    public RestResp<List<AiChatMessageRespDto>> listWriterTutorialMessages(
            @PathVariable String conversationId) {
        return aiChatHistoryService.listMessages(UserHolder.getUserId(), conversationId);
    }

    @DeleteMapping("/writer_tutorial/sessions/{conversationId}")
    public RestResp<Void> deleteWriterTutorialSession(@PathVariable String conversationId) {
        return aiChatHistoryService.deleteSession(UserHolder.getUserId(), conversationId);
    }


    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping(value = "/manus/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManus(String message) {
        MyManus manus = new MyManus(allTools, dashscopeChatModel);
        return manus.runByStream(message);
    }

}
