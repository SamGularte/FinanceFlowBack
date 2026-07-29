package com.samuelgularte.financeflow.budgeting.application.output;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.CategorySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.DailySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.MonthlyDashboard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyDashboardOutputTest {

    private final UUID userId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2026, 7, 27, 10, 0, 0);

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("should map all fields from domain to output")
        void shouldMapAllFields() {
            var tx = Transaction.create("Compra", BigDecimal.valueOf(5000, 2), Category.SUPERMARKET, userId, now);
            var dashboard = new MonthlyDashboard(
                    BigDecimal.valueOf(10000, 2),
                    5,
                    BigDecimal.valueOf(2000, 2),
                    BigDecimal.valueOf(8000, 2),
                    List.of(new CategorySpending(Category.SUPERMARKET, BigDecimal.valueOf(6000, 2), 60.0)),
                    List.of(new DailySpending(1, BigDecimal.valueOf(4000, 2))),
                    List.of(tx)
            );

            var output = MonthlyDashboardOutput.from(dashboard);

            assertEquals(BigDecimal.valueOf(10000, 2), output.totalSpent());
            assertEquals(5, output.totalTransactions());
            assertEquals(BigDecimal.valueOf(2000, 2), output.averagePerTransaction());
            assertEquals(BigDecimal.valueOf(8000, 2), output.previousMonthTotal());

            assertEquals(1, output.byCategory().size());
            assertEquals("SUPERMARKET", output.byCategory().get(0).category());
            assertEquals(BigDecimal.valueOf(6000, 2), output.byCategory().get(0).total());
            assertEquals(60.0, output.byCategory().get(0).percentage());

            assertEquals(1, output.dailyBreakdown().size());
            assertEquals(1, output.dailyBreakdown().get(0).day());
            assertEquals(BigDecimal.valueOf(4000, 2), output.dailyBreakdown().get(0).total());

            assertEquals(1, output.topTransactions().size());
            assertEquals(tx.id().toString(), output.topTransactions().get(0).id());
            assertEquals(tx.amount(), output.topTransactions().get(0).valor());
        }

        @Test
        @DisplayName("should handle empty lists")
        void shouldHandleEmptyLists() {
            var dashboard = new MonthlyDashboard(
                    BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.ZERO,
                    List.of(), List.of(), List.of()
            );

            var output = MonthlyDashboardOutput.from(dashboard);

            assertEquals(BigDecimal.ZERO, output.totalSpent());
            assertTrue(output.byCategory().isEmpty());
            assertTrue(output.dailyBreakdown().isEmpty());
            assertTrue(output.topTransactions().isEmpty());
        }
    }
}
