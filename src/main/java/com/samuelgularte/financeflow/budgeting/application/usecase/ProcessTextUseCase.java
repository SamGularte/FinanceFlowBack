package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.tool.PersistTransactionTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProcessTextUseCase {

    private final ChatClient chatClient;
    private final PersistTransactionTool persistTransactionTool;
    private final String systemPrompt;

    public ProcessTextUseCase(ChatClient chatClient, PersistTransactionTool persistTransactionTool,
                              String systemPrompt) {
        this.chatClient = chatClient;
        this.persistTransactionTool = persistTransactionTool;
        this.systemPrompt = systemPrompt;
    }

    public String execute(String userText, UUID userId) {
        log.info("Processing text for userId={}, text={}", userId, userText);
        return chatClient.prompt()
                .system(s -> s.text(systemPrompt + "\nData atual: " + LocalDateTime.now()))
                .user(userText)
                .tools(persistTransactionTool)
                .toolContext(Map.of("userId", userId.toString()))
                .call()
                .content();
    }
}
