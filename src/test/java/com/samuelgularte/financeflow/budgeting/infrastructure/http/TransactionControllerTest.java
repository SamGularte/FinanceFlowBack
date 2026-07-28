package com.samuelgularte.financeflow.budgeting.infrastructure.http;

import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetails;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetailsService;
import com.samuelgularte.financeflow.auth.infrastructure.security.JwtUtils;
import com.samuelgularte.financeflow.budgeting.application.input.UpdateTransactionInput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.application.usecase.DeleteTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.FetchUserTransactionsUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessAudioUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessTextUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.UpdateTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(TransactionControllerTest.TestConfig.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessTextUseCase processTextUseCase;

    @MockitoBean
    private ProcessAudioUseCase processAudioUseCase;

    @MockitoBean
    private FetchUserTransactionsUseCase fetchUserTransactionsUseCase;

    @MockitoBean
    private UpdateTransactionUseCase updateTransactionUseCase;

    @MockitoBean
    private DeleteTransactionUseCase deleteTransactionUseCase;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private CustomUserDetails userDetails;
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        var user = new User("test", "test@email.com", "pass");
        user.setId(userId);
        userDetails = new CustomUserDetails(user);
    }

    @Nested
    @DisplayName("POST /transactions/audio")
    class PostAudio {

        @Test
        @DisplayName("should return 200 with success message")
        void shouldReturnSuccessMessage() throws Exception {
            when(processAudioUseCase.execute(any(), eq(userId))).thenReturn("Transação registrada via áudio!");

            var audioFile = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "fake-audio".getBytes());

            mockMvc.perform(multipart("/transactions/audio")
                            .file(audioFile)
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Transação registrada via áudio!"));
        }
    }

    @Nested
    @DisplayName("GET /transactions")
    class GetTransactions {

        @Test
        @DisplayName("should return paginated transactions")
        void shouldReturnTransactions() throws Exception {
            var output = new TransactionOutput(UUID.randomUUID().toString(), "Compra", "SUPERMARKET", 50.00, "2026-07-27T10:00:00");
            var page = new PageImpl<>(java.util.List.of(output));
            when(fetchUserTransactionsUseCase.execute(eq(userId), eq(null), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/transactions")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(output.id()))
                    .andExpect(jsonPath("$.content[0].description").value("Compra"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("should filter by category when provided")
        void shouldFilterByCategory() throws Exception {
            var output = new TransactionOutput(UUID.randomUUID().toString(), "Farmácia", "PHARMACY", 15.00, "2026-07-27T10:00:00");
            var page = new PageImpl<>(java.util.List.of(output));
            when(fetchUserTransactionsUseCase.execute(eq(userId), eq(Category.PHARMACY), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/transactions")
                            .param("category", "PHARMACY")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].category").value("PHARMACY"));
        }
    }

    @Nested
    @DisplayName("PUT /transactions/{id}")
    class PutTransaction {

        @Test
        @DisplayName("should return 200 with updated transaction")
        void shouldUpdateTransaction() throws Exception {
            var output = new TransactionOutput(UUID.randomUUID().toString(), "Updated", "SUPERMARKET", 30.00, "2026-07-27T10:00:00");
            when(updateTransactionUseCase.execute(any(), eq(userId), any())).thenReturn(output);

            mockMvc.perform(put("/transactions/" + UUID.randomUUID())
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"description\":\"Updated\",\"amount\":3000,\"category\":\"SUPERMARKET\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Updated"))
                    .andExpect(jsonPath("$.category").value("SUPERMARKET"));
        }

        @Test
        @DisplayName("should return 404 when transaction not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(updateTransactionUseCase.execute(any(), eq(userId), any()))
                    .thenThrow(new EntityNotFoundException());

            mockMvc.perform(put("/transactions/" + UUID.randomUUID())
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"description\":\"Updated\",\"amount\":3000,\"category\":\"SUPERMARKET\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /transactions/{id}")
    class DeleteTransaction {

        @Test
        @DisplayName("should return 204 when deleted")
        void shouldDeleteTransaction() throws Exception {
            mockMvc.perform(delete("/transactions/" + UUID.randomUUID())
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("should return 404 when transaction not found")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new EntityNotFoundException()).when(deleteTransactionUseCase).execute(any(), eq(userId));

            mockMvc.perform(delete("/transactions/" + UUID.randomUUID())
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /transactions/text")
    class PostText {

        @Test
        @DisplayName("should return 200 with success message")
        void shouldReturnSuccessMessage() throws Exception {
            when(processTextUseCase.execute("Gastei 50 reais", userId)).thenReturn("Transação registrada!");

            mockMvc.perform(post("/transactions/text")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\":\"Gastei 50 reais\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Transação registrada!"));
        }

        @Test
        @DisplayName("should return 400 when text is blank")
        void shouldReturnBadRequestWhenTextBlank() throws Exception {
            mockMvc.perform(post("/transactions/text")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
