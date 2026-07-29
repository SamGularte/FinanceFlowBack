package com.samuelgularte.financeflow.budgeting.application.output;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

class TransactionOutputTest {

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID transactionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final Category category = Category.SUPERMARKET;
    private final LocalDateTime now = LocalDateTime.of(2026, 7, 27, 10, 0, 0);

    private Transaction transaction(BigDecimal amount, String description) {
        return new Transaction(transactionId, description, amount, category, userId, now);
    }

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("should preserve 50.00")
        void shouldPreserveValue() {
            var output = TransactionOutput.from(transaction(BigDecimal.valueOf(5000, 2), "Compra mercado"));
            assertEquals(BigDecimal.valueOf(5000, 2), output.valor());
        }

        @Test
        @DisplayName("should preserve 0.00")
        void shouldPreserveZero() {
            var output = TransactionOutput.from(transaction(BigDecimal.ZERO, "Isento"));
            assertEquals(BigDecimal.ZERO, output.valor());
        }

        @Test
        @DisplayName("should preserve negative value")
        void shouldPreserveNegative() {
            var output = TransactionOutput.from(transaction(BigDecimal.valueOf(-5000, 2), "Estorno"));
            assertEquals(BigDecimal.valueOf(-5000, 2), output.valor());
        }

        @Test
        @DisplayName("should preserve 0.01")
        void shouldPreserveOneCent() {
            var output = TransactionOutput.from(transaction(BigDecimal.valueOf(1, 2), "Taxa"));
            assertEquals(BigDecimal.valueOf(1, 2), output.valor());
        }

        @Test
        @DisplayName("should preserve 0.99")
        void shouldPreserveNinetyNineCents() {
            var output = TransactionOutput.from(transaction(BigDecimal.valueOf(99, 2), "Taxa"));
            assertThat(output.valor()).isEqualByComparingTo(BigDecimal.valueOf(99, 2));
        }

        @Test
        @DisplayName("should copy id, description and category from transaction")
        void shouldCopyFields() {
            var output = TransactionOutput.from(transaction(BigDecimal.valueOf(5000, 2), "Compra mercado"));
            assertEquals(transactionId.toString(), output.id());
            assertEquals("Compra mercado", output.description());
            assertEquals("SUPERMARKET", output.category());
        }

        @Test
        @DisplayName("should preserve null description")
        void shouldPreserveNullDescription() {
            var tx = new Transaction(transactionId, null, BigDecimal.valueOf(1000, 2), category, userId, now);
            var output = TransactionOutput.from(tx);
            assertNull(output.description());
        }

        @Test
        @DisplayName("should include createdAt in ISO format")
        void shouldIncludeCreatedAt() {
            var output = TransactionOutput.from(transaction(BigDecimal.valueOf(5000, 2), "Compra"));
            assertEquals("2026-07-27T10:00", output.createdAt());
        }
    }
}
