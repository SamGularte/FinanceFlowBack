package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMonthlyDashboardUseCaseTest {

    @Mock
    private TransactionRepository repository;

    private GetMonthlyDashboardUseCase useCase;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetMonthlyDashboardUseCase(repository);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should return dashboard with correct totals")
        void shouldReturnDashboardWithTotals() {
            when(repository.sumByUserIdAndMonth(userId, 2026, 7)).thenReturn(new BigDecimal("5000.00"));
            when(repository.countByUserIdAndMonth(userId, 2026, 7)).thenReturn(3L);
            when(repository.sumGroupByCategoryAndMonth(userId, 2026, 7))
                    .thenReturn(Map.of(Category.SUPERMARKET, new BigDecimal("3000.00"), Category.PHARMACY, new BigDecimal("2000.00")));
            when(repository.sumByDayAndMonth(userId, 2026, 7))
                    .thenReturn(Map.of(1, new BigDecimal("1500.00"), 2, new BigDecimal("3500.00")));
            when(repository.findTopByUserIdAndMonth(userId, 2026, 7, 5)).thenReturn(List.of());
            when(repository.sumByUserIdAndMonth(userId, 2026, 6)).thenReturn(new BigDecimal("4000.00"));

            MonthlyDashboard result = useCase.execute(userId, 2026, 7);

            assertThat(result.totalSpent()).isEqualByComparingTo(new BigDecimal("5000.00"));
            assertEquals(3, result.totalTransactions());
            assertThat(result.averagePerTransaction()).isEqualByComparingTo(new BigDecimal("1666.67"));
            assertThat(result.previousMonthTotal()).isEqualByComparingTo(new BigDecimal("4000.00"));
        }

        @Test
        @DisplayName("should calculate percentages correctly")
        void shouldCalculatePercentages() {
            when(repository.sumByUserIdAndMonth(userId, 2026, 7)).thenReturn(new BigDecimal("100.00"));
            when(repository.countByUserIdAndMonth(userId, 2026, 7)).thenReturn(1L);
            when(repository.sumGroupByCategoryAndMonth(userId, 2026, 7))
                    .thenReturn(Map.of(Category.SUPERMARKET, new BigDecimal("75.00"), Category.RESTAURANT, new BigDecimal("25.00")));
            when(repository.sumByDayAndMonth(userId, 2026, 7)).thenReturn(Map.of(1, new BigDecimal("100.00")));
            when(repository.findTopByUserIdAndMonth(userId, 2026, 7, 5)).thenReturn(List.of());
            when(repository.sumByUserIdAndMonth(userId, 2026, 6)).thenReturn(BigDecimal.ZERO);

            MonthlyDashboard result = useCase.execute(userId, 2026, 7);

            assertEquals(2, result.byCategory().size());
            for (var cs : result.byCategory()) {
                if (cs.category() == Category.SUPERMARKET) {
                    assertEquals(75.0, cs.percentage(), 0.01);
                } else if (cs.category() == Category.RESTAURANT) {
                    assertEquals(25.0, cs.percentage(), 0.01);
                } else {
                    fail("Unexpected category: " + cs.category());
                }
            }
        }

        @Test
        @DisplayName("should handle empty month")
        void shouldHandleEmptyMonth() {
            when(repository.sumByUserIdAndMonth(userId, 2026, 7)).thenReturn(BigDecimal.ZERO);
            when(repository.countByUserIdAndMonth(userId, 2026, 7)).thenReturn(0L);
            when(repository.sumGroupByCategoryAndMonth(userId, 2026, 7)).thenReturn(Map.of());
            when(repository.sumByDayAndMonth(userId, 2026, 7)).thenReturn(Map.of());
            when(repository.findTopByUserIdAndMonth(userId, 2026, 7, 5)).thenReturn(List.of());
            when(repository.sumByUserIdAndMonth(userId, 2026, 6)).thenReturn(BigDecimal.ZERO);

            MonthlyDashboard result = useCase.execute(userId, 2026, 7);

            assertEquals(BigDecimal.ZERO, result.totalSpent());
            assertEquals(0, result.totalTransactions());
            assertEquals(BigDecimal.ZERO, result.averagePerTransaction());
            assertTrue(result.byCategory().isEmpty());
            assertTrue(result.dailyBreakdown().isEmpty());
            assertTrue(result.topTransactions().isEmpty());
        }

        @Test
        @DisplayName("should include top transactions")
        void shouldIncludeTopTransactions() {
            var tx = Transaction.create("Compra", new BigDecimal("50.00"), Category.SUPERMARKET, userId, LocalDateTime.now());

            when(repository.sumByUserIdAndMonth(userId, 2026, 7)).thenReturn(new BigDecimal("50.00"));
            when(repository.countByUserIdAndMonth(userId, 2026, 7)).thenReturn(1L);
            when(repository.sumGroupByCategoryAndMonth(userId, 2026, 7)).thenReturn(Map.of(Category.SUPERMARKET, new BigDecimal("50.00")));
            when(repository.sumByDayAndMonth(userId, 2026, 7)).thenReturn(Map.of(1, new BigDecimal("50.00")));
            when(repository.findTopByUserIdAndMonth(userId, 2026, 7, 5)).thenReturn(List.of(tx));
            when(repository.sumByUserIdAndMonth(userId, 2026, 6)).thenReturn(BigDecimal.ZERO);

            MonthlyDashboard result = useCase.execute(userId, 2026, 7);

            assertEquals(1, result.topTransactions().size());
            assertEquals(tx.id(), result.topTransactions().get(0).id());
            assertEquals(tx.amount(), result.topTransactions().get(0).amount());
        }
    }
}
