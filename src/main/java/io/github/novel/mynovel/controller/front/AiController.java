package io.github.novel.mynovel.controller.front;

import io.github.novel.mynovel.core.ai.agent.MyManus;
import io.github.novel.mynovel.core.ai.app.WriterTutorialApp;
import io.github.novel.mynovel.core.constant.ApiRouterConsts;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_AI_URL_PREFIX)
public class AiController {

    @Resource
    private WriterTutorialApp writerTutorialApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 普通聊天
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/writer_tutorial/chat/sse")
    public SseEmitter doChatWithWriterTutorialSse(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);
        writerTutorialApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);

        return sseEmitter;
    }


    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        MyManus manus = new MyManus(allTools, dashscopeChatModel);
        return manus.runByStream(message);
    }

}
