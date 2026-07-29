package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.tool.PersistTransactionTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProcessImageUseCase {

    private final ChatClient chatClient;
    private final PersistTransactionTool persistTransactionTool;
    private final String systemPrompt;

    public ProcessImageUseCase(
            ChatClient chatClient,
            PersistTransactionTool persistTransactionTool,
            String systemPrompt) {

        this.chatClient = chatClient;
        this.persistTransactionTool = persistTransactionTool;
        this.systemPrompt = systemPrompt;
    }

    public String execute(MultipartFile imageFile, UUID userId) throws IOException {

        log.info("Processing invoice for userId={}, fileName={}",
                userId, imageFile.getOriginalFilename());

        var imageResource = new InputStreamResource(imageFile.getInputStream());

        return chatClient.prompt()
                .system(s -> s.text(systemPrompt + "\nData atual: " + LocalDateTime.now()))
                .user(u -> u
                        .text("Analise essa imagem e extraia os dados da transacao. Use a ferramenta disponivel.")
                        .media(MimeType.valueOf(imageFile.getContentType()), imageResource))
                .tools(persistTransactionTool)
                .toolContext(Map.of("userId", userId.toString()))
                .call()
                .content();
    }
}
