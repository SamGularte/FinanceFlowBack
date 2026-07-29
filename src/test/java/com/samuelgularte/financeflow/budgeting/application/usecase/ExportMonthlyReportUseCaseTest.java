package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.output.ExportReportResult;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.CategorySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.DailySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.MonthlyDashboard;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportMonthlyReportUseCaseTest {

    @Mock
    private GetMonthlyDashboardUseCase getMonthlyDashboardUseCase;

    @Mock
    private TransactionRepository transactionRepository;

    private ExportMonthlyReportUseCase useCase;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ExportMonthlyReportUseCase(getMonthlyDashboardUseCase, transactionRepository);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should generate CSV with all sections")
        void shouldGenerateCsvWithAllSections() {
            var dashboard = new MonthlyDashboard(
                    new BigDecimal("5000.00"), 3, new BigDecimal("1666.67"), new BigDecimal("4000.00"),
                    List.of(new CategorySpending(Category.SUPERMARKET, new BigDecimal("3000.00"), 60.0)),
                    List.of(new DailySpending(1, new BigDecimal("5000.00"))),
                    List.of()
            );
            when(getMonthlyDashboardUseCase.execute(userId, 2026, 7)).thenReturn(dashboard);

            var tx = Transaction.create("Compra mercado", new BigDecimal("5000.00"), Category.SUPERMARKET, userId, LocalDateTime.of(2026, 7, 5, 10, 0, 0));
            when(transactionRepository.findAllByUserIdAndMonth(userId, 2026, 7)).thenReturn(List.of(tx));

            ExportReportResult result = useCase.execute(userId, 2026, 7);

            assertThat(result.year()).isEqualTo(2026);
            assertThat(result.month()).isEqualTo(7);
            String csv = new String(result.content(), StandardCharsets.UTF_8);
            assertThat(csv).contains("RELATORIO MENSAL - julho/2026");
            assertThat(csv).contains("Total;5000,00");
            assertThat(csv).contains("Transacoes;3");
            assertThat(csv).contains("Media;1666,67");
            assertThat(csv).contains("Mes Anterior;4000,00");
            assertThat(csv).contains("Variacao;25,00%");
            assertThat(csv).contains("GASTOS POR CATEGORIA");
            assertThat(csv).contains("SUPERMARKET;3000,00;60,0%");
            assertThat(csv).contains("TRANSACOES");
            assertThat(csv).contains("05/07/2026;Compra mercado;5000,00;SUPERMARKET");
        }

        @Test
        @DisplayName("should handle empty month")
        void shouldHandleEmptyMonth() {
            var dashboard = new MonthlyDashboard(
                    BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), List.of(), List.of()
            );
            when(getMonthlyDashboardUseCase.execute(userId, 2026, 7)).thenReturn(dashboard);
            when(transactionRepository.findAllByUserIdAndMonth(userId, 2026, 7)).thenReturn(List.of());

            ExportReportResult result = useCase.execute(userId, 2026, 7);

            String csv = new String(result.content(), StandardCharsets.UTF_8);
            assertThat(csv).contains("Total;0,00");
            assertThat(csv).contains("Transacoes;0");
            assertThat(csv).contains("Variacao;0,00%");
            assertThat(csv).doesNotContain("SUPERMARKET");
        }

        @Test
        @DisplayName("should calculate variation correctly")
        void shouldCalculateVariation() {
            var dashboard = new MonthlyDashboard(
                    new BigDecimal("2000.00"), 2, new BigDecimal("1000.00"), new BigDecimal("4000.00"),
                    List.of(), List.of(), List.of()
            );
            when(getMonthlyDashboardUseCase.execute(userId, 2026, 7)).thenReturn(dashboard);
            when(transactionRepository.findAllByUserIdAndMonth(userId, 2026, 7)).thenReturn(List.of());

            ExportReportResult result = useCase.execute(userId, 2026, 7);

            String csv = new String(result.content(), StandardCharsets.UTF_8);
            assertThat(csv).contains("Variacao;-50,00%");
        }

        @Test
        @DisplayName("should use current year/month when not provided")
        void shouldDefaultYearAndMonth() {
            var dashboard = new MonthlyDashboard(
                    BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), List.of(), List.of()
            );
            when(getMonthlyDashboardUseCase.execute(eq(userId), anyInt(), anyInt())).thenReturn(dashboard);
            when(transactionRepository.findAllByUserIdAndMonth(eq(userId), anyInt(), anyInt())).thenReturn(List.of());

            ExportReportResult result = useCase.execute(userId, null, null);

            assertThat(result.content()).isNotEmpty();
            assertThat(result.year()).isGreaterThan(0);
            assertThat(result.month()).isBetween(1, 12);
        }
    }
}
