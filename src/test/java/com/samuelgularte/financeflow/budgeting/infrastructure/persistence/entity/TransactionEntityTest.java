package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity;

import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionEntityTest {

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private final LocalDateTime now = LocalDateTime.of(2026, 7, 27, 10, 0, 0);

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("should map all fields from domain to entity")
        void shouldMapAllFields() {
            Transaction transaction = Transaction.create("Compra mercado", 5000, Category.SUPERMARKET, userId, now);
            User user = new User();
            user.setId(userId);

            TransactionEntity entity = TransactionEntity.from(transaction, user);

            assertEquals(transaction.id(), entity.getId());
            assertEquals(transaction.description(), entity.getDescription());
            assertEquals(transaction.amount(), entity.getAmount());
            assertEquals(transaction.category(), entity.getCategory());
            assertEquals(userId, entity.getUser().getId());
            assertEquals(now, entity.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map all fields from entity to domain")
        void shouldMapAllFields() {
            User user = new User();
            user.setId(userId);
            UUID id = UUID.randomUUID();
            TransactionEntity entity = new TransactionEntity(id, "Farmácia", 1500, Category.PHARMACY, user, now);

            Transaction transaction = entity.toDomain();

            assertEquals(id, transaction.id());
            assertEquals("Farmácia", transaction.description());
            assertEquals(1500, transaction.amount());
            assertEquals(Category.PHARMACY, transaction.category());
            assertEquals(userId, transaction.userId());
            assertEquals(now, transaction.createdAt());
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class Roundtrip {

        @Test
        @DisplayName("should preserve all fields after from then toDomain")
        void shouldPreserveFields() {
            Transaction original = Transaction.create("Compra mercado", 5000, Category.SUPERMARKET, userId, now);
            User user = new User();
            user.setId(userId);

            TransactionEntity entity = TransactionEntity.from(original, user);
            Transaction result = entity.toDomain();

            assertEquals(original, result);
        }
    }
}
