package com.samuelgularte.financeflow.budgeting.infrastructure.http;

import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.infrastructure.security.CookieUtils;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetails;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetailsService;
import com.samuelgularte.financeflow.auth.infrastructure.security.JwtUtils;
import com.samuelgularte.financeflow.budgeting.application.usecase.request.UpdateTransactionRequest;
import com.samuelgularte.financeflow.budgeting.application.output.MonthlyDashboardOutput;
import com.samuelgularte.financeflow.budgeting.application.output.MonthlyInsightOutput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionPageMapper;
import com.samuelgularte.financeflow.budgeting.application.usecase.DeleteTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.FetchUserTransactionsUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.GenerateMonthlyInsightUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.GetMonthlyDashboardUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessAudioUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessImageUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessTextUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.UpdateTransactionUseCase;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.TransactionPage;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.CategorySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.DailySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.MonthlyDashboard;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    private ProcessImageUseCase processImageUseCase;

    @MockitoBean
    private FetchUserTransactionsUseCase fetchUserTransactionsUseCase;

    @MockitoBean
    private UpdateTransactionUseCase updateTransactionUseCase;

    @MockitoBean
    private DeleteTransactionUseCase deleteTransactionUseCase;

    @MockitoBean
    private GetMonthlyDashboardUseCase getMonthlyDashboardUseCase;

    @MockitoBean
    private GenerateMonthlyInsightUseCase generateMonthlyInsightUseCase;

    @MockitoBean
    private CookieUtils cookieUtils;

    @MockitoBean
    private TransactionPageMapper pageMapper;

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
        var user = new User(userId, "test", "test@email.com", "pass", null, null);
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
    @DisplayName("POST /transactions/image")
    class PostImage {

        @Test
        @DisplayName("should return 200 with success message")
        void shouldReturnSuccessMessage() throws Exception {
            when(processImageUseCase.execute(any(), eq(userId))).thenReturn("Transação registrada via imagem!");

            var imageFile = new MockMultipartFile("file", "receipt.jpg", "image/jpeg", "fake-image".getBytes());

            mockMvc.perform(multipart("/transactions/image")
                            .file(imageFile)
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Transação registrada via imagem!"));
        }
    }

    @Nested
    @DisplayName("GET /transactions")
    class GetTransactions {

        @Test
        @DisplayName("should return paginated transactions")
        void shouldReturnTransactions() throws Exception {
            var tx = Transaction.create("Compra", BigDecimal.valueOf(5000, 2), Category.SUPERMARKET, userId, LocalDateTime.of(2026, 7, 27, 10, 0, 0));
            var txPage = new TransactionPage(List.of(tx), 1, 0, 20);
            when(fetchUserTransactionsUseCase.execute(eq(userId), eq(null), anyInt(), anyInt())).thenReturn(txPage);

            var output = TransactionOutput.from(tx);
            var springPage = new PageImpl<>(List.of(output), PageRequest.of(0, 20), 1);
            when(pageMapper.toSpringPage(txPage)).thenReturn(springPage);

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
            var tx = Transaction.create("Farmácia", BigDecimal.valueOf(1500, 2), Category.PHARMACY, userId, LocalDateTime.of(2026, 7, 27, 10, 0, 0));
            var txPage = new TransactionPage(List.of(tx), 1, 0, 20);
            when(fetchUserTransactionsUseCase.execute(eq(userId), eq(Category.PHARMACY), anyInt(), anyInt())).thenReturn(txPage);

            var output = TransactionOutput.from(tx);
            var springPage = new PageImpl<>(List.of(output), PageRequest.of(0, 20), 1);
            when(pageMapper.toSpringPage(txPage)).thenReturn(springPage);

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
            var output = new TransactionOutput(UUID.randomUUID().toString(), "Updated", "SUPERMARKET", BigDecimal.valueOf(3000, 2), "2026-07-27T10:00:00");
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

    @Nested
    @DisplayName("POST /transactions/insights")
    class PostInsights {

        @Test
        @DisplayName("should return 200 with insight content")
        void shouldReturnInsight() throws Exception {
            var insight = new MonthlyInsight(
                    UUID.randomUUID(), userId, 2026, 7,
                    "Insight gerado pelo Gemini.",
                    LocalDateTime.of(2026, 7, 29, 10, 0, 0)
            );
            when(generateMonthlyInsightUseCase.execute(eq(userId), eq(2026), eq(7))).thenReturn(insight);

            mockMvc.perform(post("/transactions/insights")
                            .param("year", "2026")
                            .param("month", "7")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Insight gerado pelo Gemini."))
                    .andExpect(jsonPath("$.generatedAt").value("2026-07-29T10:00:00"));
        }

        @Test
        @DisplayName("should default year and month when not provided")
        void shouldDefaultYearAndMonth() throws Exception {
            var insight = new MonthlyInsight(
                    UUID.randomUUID(), userId, 2026, 7,
                    "Insight padrão.",
                    LocalDateTime.now()
            );
            when(generateMonthlyInsightUseCase.execute(eq(userId), eq(null), eq(null))).thenReturn(insight);

            mockMvc.perform(post("/transactions/insights")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("Insight padrão."));
        }
    }

    @Nested
    @DisplayName("GET /transactions/dashboard/monthly")
    class GetDashboard {

        @Test
        @DisplayName("should return 200 with dashboard data")
        void shouldReturnDashboard() throws Exception {
            var dashboard = new MonthlyDashboard(
                    BigDecimal.valueOf(5000, 2), 10, BigDecimal.valueOf(500, 2), BigDecimal.valueOf(4000, 2),
                    List.of(new CategorySpending(Category.SUPERMARKET, BigDecimal.valueOf(3000, 2), 60.0)),
                    List.of(new DailySpending(1, BigDecimal.valueOf(2000, 2))),
                    List.of()
            );
            when(getMonthlyDashboardUseCase.execute(eq(userId), eq(null), eq(null))).thenReturn(dashboard);

            mockMvc.perform(get("/transactions/dashboard/monthly")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSpent").value(50.00))
                    .andExpect(jsonPath("$.totalTransactions").value(10))
                    .andExpect(jsonPath("$.byCategory[0].category").value("SUPERMARKET"))
                    .andExpect(jsonPath("$.dailyBreakdown[0].day").value(1));
        }

        @Test
        @DisplayName("should pass year and month params to use case")
        void shouldPassYearAndMonthParams() throws Exception {
            var dashboard = new MonthlyDashboard(
                    BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), List.of(), List.of()
            );
            when(getMonthlyDashboardUseCase.execute(eq(userId), eq(2026), eq(7))).thenReturn(dashboard);

            mockMvc.perform(get("/transactions/dashboard/monthly")
                            .param("year", "2026")
                            .param("month", "7")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk());
        }
    }
}
