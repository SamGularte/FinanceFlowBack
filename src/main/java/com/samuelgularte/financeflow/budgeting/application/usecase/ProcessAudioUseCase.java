package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.tool.PersistTransactionTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ProcessAudioUseCase {

    private final ChatClient chatClient;
    private final PersistTransactionTool persistTransactionTool;
    private final String systemPrompt;

    public ProcessAudioUseCase(ChatClient chatClient, PersistTransactionTool persistTransactionTool,
                               @Value("classpath:budgeting/prompts/system-prompt.st") Resource systemPromptResource) throws IOException {
        this.chatClient = chatClient;
        this.persistTransactionTool = persistTransactionTool;
        this.systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    public String execute(MultipartFile audioFile, UUID userId) throws IOException {
        var audioResource = new InputStreamResource(audioFile.getInputStream());
        return chatClient.prompt()
                .system(s -> s.text(systemPrompt + "\nData atual: " + LocalDateTime.now()))
                .user(u -> u
                        .text("Transcreva o audio e extraia os dados da transacao. Use a ferramenta disponivel.")
                        .media(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM, audioResource))
                .tools(persistTransactionTool)
                .toolContext(Map.of("userId", userId.toString()))
                .call()
                .content();
    }
}
