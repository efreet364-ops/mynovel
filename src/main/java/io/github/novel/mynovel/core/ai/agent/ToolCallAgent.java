package io.github.novel.mynovel.core.ai.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类  
 */  
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {  
  
    // 可用的工具  
    private final ToolCallback[] availableTools;
  
    // 保存了工具调用信息的响应  
    private ChatResponse toolCallChatResponse;

    private String lastAssistantText;

    private List<AssistantMessage.ToolCall> lastToolCallList = List.of();
  
    // 工具调用管理者  
    private final ToolCallingManager toolCallingManager;
  
    // 禁用内置的工具调用机制，自己维护上下文  
    private final ChatOptions chatOptions;
  
    public ToolCallAgent(ToolCallback[] availableTools) {  
        super();  
        this.availableTools = availableTools;  
        this.toolCallingManager = ToolCallingManager.builder().build();  
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文  
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();  
    }

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                if (lastAssistantText != null && !lastAssistantText.isBlank()) {
                    return "### 最终回答\n\n" + lastAssistantText;
                }
                return "### 思考完成\n\n当前任务不需要继续调用工具。";
            }
            return act();
        } catch (Exception e) {
            log.error("步骤执行失败", e);
            return "### 执行失败\n\n" + e.getMessage();
        }
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessagesList().add(userMessage);
        }
        List<Message> messageList = getMessagesList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // 获取带工具选项的响应
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于 Act
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 输出提示信息
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            this.lastAssistantText = result;
            this.lastToolCallList = toolCallList == null ? List.of() : toolCallList;
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s",
                            toolCall.name(),
                            toolCall.arguments())
                    )
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCallList.isEmpty()) {
                // 没有工具调用代表模型认为当前任务已经完成
                getMessagesList().add(assistantMessage);
                setState(AgentState.FINISHED);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessagesList().add(
                    new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }


    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessagesList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessagesList(toolExecutionResult.conversationHistory());
        // 当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String toolPlan = lastToolCallList.stream()
                .map(toolCall -> "- `" + toolCall.name() + "` 参数：" + summarizeToolArguments(toolCall.arguments()))
                .collect(Collectors.joining("\n"));
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "- `" + response.name() + "`：" + summarizeToolResponse(response.responseData()))
                .collect(Collectors.joining("\n"));
        boolean terminated = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminated) {
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return """
                ### Step %d

                **处理进度**

                %s

                **使用工具**

                %s

                **执行结果**

                %s
                """.formatted(
                getCurrentStep(),
                hasText(lastAssistantText) ? lastAssistantText : "我需要调用工具获取更多信息。",
                hasText(toolPlan) ? toolPlan : "- 无",
                hasText(results) ? results : "- 工具没有返回可展示内容"
        );
    }

    private String summarizeToolArguments(String arguments) {
        String normalized = normalize(arguments);
        return truncate(normalized, 220);
    }

    private String summarizeToolResponse(String responseData) {
        String normalized = normalize(responseData);
        if (!hasText(normalized)) {
            return "没有返回内容。";
        }
        if (normalized.startsWith("Error") || normalized.startsWith("错误")) {
            return truncate(normalized, 260);
        }

        List<String> titles = extractJsonValues(normalized, "title", 5);
        List<String> snippets = extractJsonValues(normalized, "snippet", 1);
        if (!titles.isEmpty()) {
            String titleSummary = titles.stream()
                    .map(title -> "《" + truncate(title, 60) + "》")
                    .collect(Collectors.joining("、"));
            String snippetSummary = snippets.isEmpty() ? "" : "。摘要：" + truncate(snippets.getFirst(), 180);
            return "已返回 " + titles.size() + " 条可用结果：" + titleSummary + snippetSummary;
        }

        return truncate(normalized, 420);
    }

    private List<String> extractJsonValues(String text, String key, int limit) {
        List<String> values = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\\\?\"" + Pattern.quote(key) + "\\\\?\"\\s*:\\s*\\\\?\"(.*?)(?<!\\\\)\\\\?\"");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find() && values.size() < limit) {
            String value = matcher.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .trim();
            if (hasText(value)) {
                values.add(value);
            }
        }
        return values;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }


}
