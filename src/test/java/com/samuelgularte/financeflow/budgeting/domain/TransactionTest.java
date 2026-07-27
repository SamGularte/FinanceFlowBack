package com.samuelgularte.financeflow.budgeting.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should generate a non-null UUID")
        void shouldGenerateNonNullId() {
            Transaction transaction = Transaction.create("Compra mercado", 5000, Category.SUPERMARKET, userId);
            assertNotNull(transaction.id());
        }

        @Test
        @DisplayName("should set description, amount and category correctly")
        void shouldSetFieldsCorrectly() {
            Transaction transaction = Transaction.create("Compra mercado", 5000, Category.SUPERMARKET, userId);

            assertEquals("Compra mercado", transaction.description());
            assertEquals(5000, transaction.amount());
            assertEquals(Category.SUPERMARKET, transaction.category());
            assertEquals(userId, transaction.userId());
        }

        @Test
        @DisplayName("should generate unique IDs for each transaction")
        void shouldGenerateUniqueIds() {
            Transaction tx1 = Transaction.create("Compra mercado", 5000, Category.SUPERMARKET, userId);
            Transaction tx2 = Transaction.create("Farmácia", 1500, Category.PHARMACY, userId);

            assertNotEquals(tx1.id(), tx2.id());
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should allow zero amount")
        void shouldAllowZeroAmount() {
            Transaction transaction = Transaction.create("Isento", 0, Category.OTHER, userId);

            assertEquals(0, transaction.amount());
        }

        @Test
        @DisplayName("should allow negative amount")
        void shouldAllowNegativeAmount() {
            Transaction transaction = Transaction.create("Estorno", -5000, Category.OTHER, userId);

            assertEquals(-5000, transaction.amount());
        }

        @Test
        @DisplayName("should allow empty description")
        void shouldAllowEmptyDescription() {
            Transaction transaction = Transaction.create("", 1000, Category.OTHER, userId);

            assertEquals("", transaction.description());
        }

        @Test
        @DisplayName("should allow null description")
        void shouldAllowNullDescription() {
            Transaction transaction = Transaction.create(null, 1000, Category.OTHER, userId);

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
                    "Compra", 1000, Category.OTHER, userId
            );
            Transaction tx2 = new Transaction(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Compra", 1000, Category.OTHER, userId
            );

            assertEquals(tx1, tx2);
        }

        @Test
        @DisplayName("should implement hashCode based on all fields")
        void shouldImplementHashCode() {
            Transaction tx1 = new Transaction(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Compra", 1000, Category.OTHER, userId
            );
            Transaction tx2 = new Transaction(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    "Compra", 1000, Category.OTHER, userId
            );

            assertEquals(tx1.hashCode(), tx2.hashCode());
        }
    }
}
