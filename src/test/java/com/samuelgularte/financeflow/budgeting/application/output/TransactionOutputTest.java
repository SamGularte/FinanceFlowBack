package com.samuelgularte.financeflow.budgeting.application.output;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionOutputTest {

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID transactionId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final Category category = Category.SUPERMARKET;
    private final LocalDateTime now = LocalDateTime.of(2026, 7, 27, 10, 0, 0);

    private Transaction transaction(long amount, String description) {
        return new Transaction(transactionId, description, amount, category, userId, now);
    }

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("should convert 5000 cents to 50.0")
        void shouldConvertCentsToReais() {
            var output = TransactionOutput.from(transaction(5000, "Compra mercado"));
            assertEquals(50.0, output.valor(), 0.001);
        }

        @Test
        @DisplayName("should convert 0 cents to 0.0")
        void shouldConvertZeroCents() {
            var output = TransactionOutput.from(transaction(0, "Isento"));
            assertEquals(0.0, output.valor(), 0.001);
        }

        @Test
        @DisplayName("should convert negative cents to negative reais")
        void shouldConvertNegativeCents() {
            var output = TransactionOutput.from(transaction(-5000, "Estorno"));
            assertEquals(-50.0, output.valor(), 0.001);
        }

        @Test
        @DisplayName("should convert 1 cent to 0.01")
        void shouldConvertOneCent() {
            var output = TransactionOutput.from(transaction(1, "Taxa"));
            assertEquals(0.01, output.valor(), 0.001);
        }

        @Test
        @DisplayName("should convert 99 cents to 0.99")
        void shouldConvertNinetyNineCents() {
            var output = TransactionOutput.from(transaction(99, "Taxa"));
            assertEquals(0.99, output.valor(), 0.001);
        }

        @Test
        @DisplayName("should convert Long.MAX_VALUE cents without overflow")
        void shouldConvertMaxLong() {
            var output = TransactionOutput.from(transaction(Long.MAX_VALUE, "Grande"));
            assertEquals(9.223372036854776E16, output.valor(), 0.001);
            assertTrue(output.valor() > 0);
        }

        @Test
        @DisplayName("should convert Long.MIN_VALUE cents without overflow")
        void shouldConvertMinLong() {
            var output = TransactionOutput.from(transaction(Long.MIN_VALUE, "Mínimo"));
            assertEquals(-9.223372036854776E16, output.valor(), 0.001);
            assertTrue(output.valor() < 0);
        }

        @Test
        @DisplayName("should copy id, description and category from transaction")
        void shouldCopyFields() {
            var output = TransactionOutput.from(transaction(5000, "Compra mercado"));
            assertEquals(transactionId.toString(), output.id());
            assertEquals("Compra mercado", output.description());
            assertEquals("SUPERMARKET", output.category());
        }

        @Test
        @DisplayName("should preserve null description")
        void shouldPreserveNullDescription() {
            var tx = new Transaction(transactionId, null, 1000, category, userId, now);
            var output = TransactionOutput.from(tx);
            assertNull(output.description());
        }

        @Test
        @DisplayName("should include createdAt in ISO format")
        void shouldIncludeCreatedAt() {
            var output = TransactionOutput.from(transaction(5000, "Compra"));
            assertEquals("2026-07-27T10:00", output.createdAt());
        }
    }
}
