package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityTest {

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("should map all fields from domain to entity")
        void shouldMapAllFields() {
            Transaction transaction = Transaction.create("Compra mercado", 5000, Category.SUPERMARKET);

            TransactionEntity entity = TransactionEntity.from(transaction);

            assertEquals(transaction.id(), entity.getId());
            assertEquals(transaction.description(), entity.getDescription());
            assertEquals(transaction.amount(), entity.getAmount());
            assertEquals(transaction.category(), entity.getCategory());
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map all fields from entity to domain")
        void shouldMapAllFields() {
            UUID id = UUID.randomUUID();
            TransactionEntity entity = new TransactionEntity(id, "Farmácia", 1500, Category.PHARMACY);

            Transaction transaction = entity.toDomain();

            assertEquals(id, transaction.id());
            assertEquals("Farmácia", transaction.description());
            assertEquals(1500, transaction.amount());
            assertEquals(Category.PHARMACY, transaction.category());
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class Roundtrip {

        @Test
        @DisplayName("should preserve all fields after from then toDomain")
        void shouldPreserveFields() {
            Transaction original = Transaction.create("Compra mercado", 5000, Category.SUPERMARKET);

            TransactionEntity entity = TransactionEntity.from(original);
            Transaction result = entity.toDomain();

            assertEquals(original, result);
        }
    }
}
