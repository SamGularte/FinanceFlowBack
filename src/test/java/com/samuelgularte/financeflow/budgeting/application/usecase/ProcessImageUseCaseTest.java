package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.tool.PersistTransactionTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessImageUseCaseTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private PersistTransactionTool persistTransactionTool;

    @Mock
    private MultipartFile imageFile;

    private ProcessImageUseCase useCase;
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() throws Exception {
        useCase = new ProcessImageUseCase(chatClient, persistTransactionTool, "Você é um assistente financeiro.");
        when(imageFile.getInputStream()).thenReturn(new ByteArrayInputStream("fake-image".getBytes()));
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should call ChatClient with image media and return content")
        void shouldCallChatClientWithImage() throws Exception {
            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.system(any(Consumer.class))).thenReturn(requestSpec);
            when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
            when(requestSpec.tools(any())).thenReturn(requestSpec);
            when(requestSpec.toolContext(any())).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("Transação registrada via imagem");

            String result = useCase.execute(imageFile, userId);

            assertEquals("Transação registrada via imagem", result);
            verify(requestSpec).system(any(Consumer.class));
            verify(requestSpec).user(any(Consumer.class));
            verify(requestSpec).tools(persistTransactionTool);
            verify(requestSpec).toolContext(any());
            verify(callResponseSpec).content();
        }

        @Test
        @DisplayName("should return null when ChatClient returns null")
        void shouldReturnNullWhenContentNull() throws Exception {
            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.system(any(Consumer.class))).thenReturn(requestSpec);
            when(requestSpec.user(any(Consumer.class))).thenReturn(requestSpec);
            when(requestSpec.tools(any())).thenReturn(requestSpec);
            when(requestSpec.toolContext(any())).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn(null);

            String result = useCase.execute(imageFile, userId);

            assertNull(result);
        }
    }
}
