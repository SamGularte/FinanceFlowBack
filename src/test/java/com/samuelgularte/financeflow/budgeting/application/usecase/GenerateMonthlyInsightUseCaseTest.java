package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.MonthlyDashboard;
import com.samuelgularte.financeflow.budgeting.domain.repository.MonthlyInsightRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.i18n.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateMonthlyInsightUseCaseTest {

    @Mock
    private MonthlyInsightRepository insightRepository;

    @Mock
    private GetMonthlyDashboardUseCase getMonthlyDashboardUseCase;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private Messages messages;

    private GenerateMonthlyInsightUseCase useCase;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GenerateMonthlyInsightUseCase(insightRepository, getMonthlyDashboardUseCase, chatClient, messages);
    }

    private void mockChatClient(String content) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(content);
    }

    private void mockDashboard() {
        when(getMonthlyDashboardUseCase.execute(any(), anyInt(), anyInt()))
                .thenReturn(new MonthlyDashboard(
                        BigDecimal.valueOf(5000), 5, BigDecimal.valueOf(1000), BigDecimal.valueOf(4000),
                        List.of(), List.of(), List.of()
                ));
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should return cached insight for closed month generated on last day")
        void shouldReturnCachedForClosedMonthOnLastDay() {
            var cached = new MonthlyInsight(
                    UUID.randomUUID(), userId, 2026, 5,
                    "Insight de maio", LocalDateTime.of(2026, 5, 31, 10, 0, 0)
            );
            when(insightRepository.findByUserIdAndYearAndMonth(userId, 2026, 5)).thenReturn(Optional.of(cached));

            MonthlyInsight result = useCase.execute(userId, 2026, 5);

            assertThat(result).isEqualTo(cached);
            verify(insightRepository, never()).save(any());
            verifyNoInteractions(chatClient);
        }

        @Test
        @DisplayName("should regenerate for closed month when insight was not on last day")
        void shouldRegenerateForClosedMonthNotOnLastDay() {
            UUID existingId = UUID.randomUUID();
            var existing = new MonthlyInsight(
                    existingId, userId, 2026, 5,
                    "Insight antigo", LocalDateTime.of(2026, 5, 15, 10, 0, 0)
            );
            when(insightRepository.findByUserIdAndYearAndMonth(userId, 2026, 5)).thenReturn(Optional.of(existing));
            mockDashboard();
            mockChatClient("Novo insight de maio");
            when(insightRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            MonthlyInsight result = useCase.execute(userId, 2026, 5);

            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.content()).isEqualTo("Novo insight de maio");
            verify(insightRepository).save(argThat(i -> i.id().equals(existingId)));
        }

        @Test
        @DisplayName("should return cached insight for current month generated today")
        void shouldReturnCachedForCurrentMonthGeneratedToday() {
            LocalDate today = LocalDate.now();
            var cached = new MonthlyInsight(
                    UUID.randomUUID(), userId, today.getYear(), today.getMonthValue(),
                    "Insight de hoje", LocalDateTime.now()
            );
            when(insightRepository.findByUserIdAndYearAndMonth(userId, today.getYear(), today.getMonthValue()))
                    .thenReturn(Optional.of(cached));

            MonthlyInsight result = useCase.execute(userId, null, null);

            assertThat(result).isEqualTo(cached);
            verify(insightRepository, never()).save(any());
            verifyNoInteractions(chatClient);
        }

        @Test
        @DisplayName("should regenerate for current month when insight was generated yesterday")
        void shouldRegenerateForCurrentMonthGeneratedYesterday() {
            LocalDate today = LocalDate.now();
            UUID existingId = UUID.randomUUID();
            var existing = new MonthlyInsight(
                    existingId, userId, today.getYear(), today.getMonthValue(),
                    "Insight de ontem", LocalDateTime.now().minusDays(1)
            );
            when(insightRepository.findByUserIdAndYearAndMonth(userId, today.getYear(), today.getMonthValue()))
                    .thenReturn(Optional.of(existing));
            mockDashboard();
            mockChatClient("Insight atualizado");
            when(insightRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            MonthlyInsight result = useCase.execute(userId, null, null);

            assertThat(result.id()).isEqualTo(existingId);
            assertThat(result.content()).isEqualTo("Insight atualizado");
            verify(insightRepository).save(argThat(i -> i.id().equals(existingId)));
        }

        @Test
        @DisplayName("should generate new insight when none exists")
        void shouldGenerateNewWhenNoneExists() {
            when(insightRepository.findByUserIdAndYearAndMonth(userId, 2026, 7)).thenReturn(Optional.empty());
            mockDashboard();
            mockChatClient("Primeiro insight");
            when(insightRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            MonthlyInsight result = useCase.execute(userId, 2026, 7);

            assertThat(result.content()).isEqualTo("Primeiro insight");
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.year()).isEqualTo(2026);
            assertThat(result.month()).isEqualTo(7);
            verify(insightRepository).save(argThat(i -> i.id() != null));
        }

        @Test
        @DisplayName("should not call Gemini when there are fewer than 5 transactions")
        void shouldNotCallGeminiForFewTransactions() {
            when(insightRepository.findByUserIdAndYearAndMonth(userId, 2026, 7)).thenReturn(Optional.empty());
            when(getMonthlyDashboardUseCase.execute(any(), anyInt(), anyInt()))
                    .thenReturn(new MonthlyDashboard(
                            BigDecimal.valueOf(100), 4, BigDecimal.valueOf(25), BigDecimal.ZERO,
                            List.of(), List.of(), List.of()
                    ));
            when(messages.get("insight.few-transactions")).thenReturn("Poucas transações.");
            when(insightRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            MonthlyInsight result = useCase.execute(userId, 2026, 7);

            assertThat(result.content()).isEqualTo("Poucas transações.");
            verifyNoInteractions(chatClient);
        }

        @Test
        @DisplayName("should default year and month to current when not provided")
        void shouldDefaultYearAndMonth() {
            LocalDate today = LocalDate.now();
            when(insightRepository.findByUserIdAndYearAndMonth(userId, today.getYear(), today.getMonthValue()))
                    .thenReturn(Optional.empty());
            mockDashboard();
            mockChatClient("Insight do mês atual");
            when(insightRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            MonthlyInsight result = useCase.execute(userId, null, null);

            assertThat(result.year()).isEqualTo(today.getYear());
            assertThat(result.month()).isEqualTo(today.getMonthValue());
        }
    }
}
