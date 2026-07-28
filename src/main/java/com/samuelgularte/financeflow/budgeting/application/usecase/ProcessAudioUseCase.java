package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.tool.PersistTransactionTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProcessAudioUseCase {

    private final ChatClient chatClient;
    private final PersistTransactionTool persistTransactionTool;
    private final String systemPrompt;

    public ProcessAudioUseCase(ChatClient chatClient, PersistTransactionTool persistTransactionTool,
                               String systemPrompt) {
        this.chatClient = chatClient;
        this.persistTransactionTool = persistTransactionTool;
        this.systemPrompt = systemPrompt;
    }

    public String execute(MultipartFile audioFile, UUID userId) throws IOException {
        log.info("Processing audio for userId={}, fileName={}", userId, audioFile.getOriginalFilename());
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
