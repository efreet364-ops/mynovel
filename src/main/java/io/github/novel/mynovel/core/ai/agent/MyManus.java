package io.github.novel.mynovel.core.ai.agent;

import io.github.novel.mynovel.core.ai.advisors.MyLoggerAdvisor;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class MyManus extends ToolCallAgent {

  
    public MyManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);  
        this.setName("MyManus");
        String SYSTEM_PROMPT = """  
                You are MyManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """;  
        this.setSystemPrompt(SYSTEM_PROMPT);  
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                Before calling tools, provide only a concise user-facing progress update, not hidden reasoning.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                Always continue the conversation in the same language as the user's latest message unless instructed otherwise.
                """;  
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化客户端  
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }  
}
