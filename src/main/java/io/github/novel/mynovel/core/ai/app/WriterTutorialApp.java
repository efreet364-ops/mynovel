package io.github.novel.mynovel.core.ai.app;

import io.github.novel.mynovel.core.ai.advisors.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

@Component
public class WriterTutorialApp {

    private static final String SYSTEM_PROMPT = """
            扮演深耕写作领域的专家。开场向用户表名身份，告知用户可以解决写作小说相关的难题。
            """;

    private final ChatClient chatclient;

    private final ChatMemory chatMemory;

    public WriterTutorialApp(ChatModel dashScopeChatModel) {
        chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(5)
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .build();

        chatclient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatclient.prompt()
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).conversationId(chatId).build())
                .call()
                .chatResponse();

        return chatResponse.getResult().getOutput().getText();
    }
}
