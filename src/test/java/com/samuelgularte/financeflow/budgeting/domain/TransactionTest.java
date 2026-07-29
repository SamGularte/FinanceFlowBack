package com.samuelgularte.financeflow.budgeting.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private final LocalDateTime now = LocalDateTime.of(2026, 7, 27, 10, 0, 0);

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should generate a non-null UUID")
        void shouldGenerateNonNullId() {
            Transaction transaction = Transaction.create("Compra mercado", BigDecimal.valueOf(5000, 2), Category.SUPERMARKET, userId);
            assertNotNull(transaction.id());
        }

        @Test
        @DisplayName("should set description, amount and category correctly")
        void shouldSetFieldsCorrectly() {
            Transaction transaction = Transaction.create("Compra mercado", BigDecimal.valueOf(5000, 2), Category.SUPERMARKET, userId);

            assertEquals("Compra mercado", transaction.description());
            assertEquals(BigDecimal.valueOf(5000, 2), transaction.amount());
            assertEquals(Category.SUPERMARKET, transaction.category());
            assertEquals(userId, transaction.userId());
            assertNotNull(transaction.createdAt());
        }

        @Test
        @DisplayName("should generate unique IDs for each transaction")
        void shouldGenerateUniqueIds() {
            Transaction tx1 = Transaction.create("Compra mercado", BigDecimal.valueOf(5000, 2), Category.SUPERMARKET, userId);
            Transaction tx2 = Transaction.create("Farmácia", BigDecimal.valueOf(1500, 2), Category.PHARMACY, userId);

            assertNotEquals(tx1.id(), tx2.id());
        }

        @Test
        @DisplayName("should accept custom createdAt")
        void shouldAcceptCustomCreatedAt() {
            Transaction transaction = Transaction.create("Compra", BigDecimal.valueOf(5000, 2), Category.SUPERMARKET, userId, now);

            assertEquals(now, transaction.createdAt());
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should allow zero amount")
        void shouldAllowZeroAmount() {
            Transaction transaction = Transaction.create("Isento", BigDecimal.ZERO, Category.OTHER, userId);

            assertEquals(BigDecimal.ZERO, transaction.amount());
        }

        @Test
        @DisplayName("should allow negative amount")
        void shouldAllowNegativeAmount() {
            Transaction transaction = Transaction.create("Estorno", BigDecimal.valueOf(-5000, 2), Category.OTHER, userId);

            assertEquals(BigDecimal.valueOf(-5000, 2), transaction.amount());
        }

        @Test
        @DisplayName("should allow empty description")
        void shouldAllowEmptyDescription() {
            Transaction transaction = Transaction.create("", BigDecimal.valueOf(1000, 2), Category.OTHER, userId);

            assertEquals("", transaction.description());
        }

        @Test
        @DisplayName("should allow null description")
        void shouldAllowNullDescription() {
            Transaction transaction = Transaction.create(null, BigDecimal.valueOf(1000, 2), Category.OTHER, userId);

            assertNull(transaction.description());
        }
    }

    @Nested
    @DisplayName("record")
    class Record {

        @Test
        @DisplayName("should implement equals based on all fields")
        void shouldImplementEquals() {
            Transaction tx1 = new Transaction(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Compra", BigDecimal.valueOf(1000, 2), Category.OTHER, userId, now
            );
            Transaction tx2 = new Transaction(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Compra", BigDecimal.valueOf(1000, 2), Category.OTHER, userId, now
            );

            assertEquals(tx1, tx2);
        }

        @Test
        @DisplayName("should implement hashCode based on all fields")
        void shouldImplementHashCode() {
            Transaction tx1 = new Transaction(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Compra", BigDecimal.valueOf(1000, 2), Category.OTHER, userId, now
            );
            Transaction tx2 = new Transaction(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Compra", BigDecimal.valueOf(1000, 2), Category.OTHER, userId, now
            );

            assertEquals(tx1.hashCode(), tx2.hashCode());
        }
    }
}
