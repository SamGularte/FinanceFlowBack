package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.tool.PersistTransactionTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class ProcessTextUseCase {

    private final ChatClient chatClient;
    private final PersistTransactionTool persistTransactionTool;
    private final String systemPrompt;

    public ProcessTextUseCase(ChatClient chatClient, PersistTransactionTool persistTransactionTool,
                              @Value("classpath:budgeting/prompts/system-prompt.st") Resource systemPromptResource) throws IOException {
        this.chatClient = chatClient;
        this.persistTransactionTool = persistTransactionTool;
        this.systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    public String execute(String userText, UUID userId) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userText)
                .tools(persistTransactionTool)
                .toolContext(Map.of("userId", userId.toString()))
                .call()
                .content();
    }
}
