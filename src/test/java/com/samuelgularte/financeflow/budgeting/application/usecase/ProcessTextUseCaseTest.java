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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessTextUseCaseTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private PersistTransactionTool persistTransactionTool;

    private ProcessTextUseCase useCase;
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() throws Exception {
        Resource systemPrompt = new ByteArrayResource("Você é um assistente financeiro.".getBytes());
        useCase = new ProcessTextUseCase(chatClient, persistTransactionTool, systemPrompt);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should return null when ChatClient returns null content")
        void shouldReturnNullWhenContentNull() {
            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.system(anyString())).thenReturn(requestSpec);
            when(requestSpec.user(anyString())).thenReturn(requestSpec);
            when(requestSpec.tools(any())).thenReturn(requestSpec);
            when(requestSpec.toolContext(any())).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn(null);

            String result = useCase.execute("Gastei 50 reais", userId);

            assertNull(result);
        }

        @Test
        @DisplayName("should call ChatClient with system prompt, user text and toolContext")
        void shouldCallChatClientWithToolContext() {
            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.system(anyString())).thenReturn(requestSpec);
            when(requestSpec.user(anyString())).thenReturn(requestSpec);
            when(requestSpec.tools(any())).thenReturn(requestSpec);
            when(requestSpec.toolContext(any())).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(callResponseSpec);
            when(callResponseSpec.content()).thenReturn("Transação registrada com sucesso");

            String result = useCase.execute("Gastei 50 reais em comida", userId);

            assertEquals("Transação registrada com sucesso", result);
            verify(requestSpec).system("Você é um assistente financeiro.");
            verify(requestSpec).user("Gastei 50 reais em comida");
            verify(requestSpec).tools(persistTransactionTool);
            verify(requestSpec).toolContext(Map.of("userId", userId.toString()));
        }
    }
}
