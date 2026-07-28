package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.TransactionPage;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TransactionRepositoryImpl.class)
class TransactionRepositoryImplTest {

    @Autowired
    private TransactionRepositoryImpl repository;

    @Autowired
    private EntityManager entityManager;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity(UUID.randomUUID(), "testuser", "test@email.com", "password", null, null);
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist and return the transaction")
        void shouldPersistAndReturn() {
            Transaction transaction = Transaction.create("Compra mercado", 5000, Category.SUPERMARKET, testUser.getId());

            Transaction saved = repository.save(transaction);

            assertNotNull(saved.id());
            assertEquals(transaction.description(), saved.description());
            assertEquals(transaction.amount(), saved.amount());
            assertEquals(transaction.category(), saved.category());
            assertEquals(testUser.getId(), saved.userId());
        }
    }

    @Nested
    @DisplayName("save updating existing")
    class SaveUpdate {

        @Test
        @DisplayName("should update fields when saving with existing ID")
        void shouldUpdateExistingTransaction() {
            Transaction transaction = repository.save(
                    Transaction.create("Compra mercado", 5000, Category.SUPERMARKET, testUser.getId()));

            Transaction updated = new Transaction(
                    transaction.id(),
                    "Compra mercado atualizada",
                    6000,
                    Category.SUPERMARKET,
                    testUser.getId(),
                    transaction.createdAt()
            );
            Transaction result = repository.save(updated);

            assertEquals(transaction.id(), result.id());
            assertEquals("Compra mercado atualizada", result.description());
            assertEquals(6000, result.amount());
        }
    }

    @Nested
    @DisplayName("findAllByCategory")
    class FindAllByCategory {

        @Test
        @DisplayName("should return transactions matching the category")
        void shouldReturnMatchingTransactions() {
            Transaction remedio = repository.save(
                    Transaction.create("Farmácia", 1500, Category.PHARMACY, testUser.getId()));
            repository.save(Transaction.create("Mercado", 5000, Category.SUPERMARKET, testUser.getId()));

            TransactionPage result = repository.findAllByCategory(Category.PHARMACY, 0, 20);

            assertEquals(1, result.totalElements());
            assertEquals(remedio.id(), result.content().get(0).id());
        }

        @Test
        @DisplayName("should return empty page when no transaction matches")
        void shouldReturnEmptyWhenNoMatch() {
            repository.save(Transaction.create("Compra mercado", 5000, Category.SUPERMARKET, testUser.getId()));

            TransactionPage result = repository.findAllByCategory(Category.AUTO, 0, 20);

            assertTrue(result.content().isEmpty());
        }

        @Test
        @DisplayName("should return multiple transactions of the same category")
        void shouldReturnMultipleMatchingTransactions() {
            repository.save(Transaction.create("Farmácia 1", 1000, Category.PHARMACY, testUser.getId()));
            repository.save(Transaction.create("Farmácia 2", 2000, Category.PHARMACY, testUser.getId()));
            repository.save(Transaction.create("Mercado", 5000, Category.SUPERMARKET, testUser.getId()));

            TransactionPage result = repository.findAllByCategory(Category.PHARMACY, 0, 20);

            assertEquals(2, result.totalElements());
        }
    }

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {

        @Test
        @DisplayName("should return transaction when found")
        void shouldReturnTransaction() {
            Transaction tx = repository.save(
                    Transaction.create("Compra", 1000, Category.OTHER, testUser.getId()));

            var found = repository.findByIdAndUserId(tx.id(), testUser.getId());

            assertTrue(found.isPresent());
            assertEquals(tx.id(), found.get().id());
        }

        @Test
        @DisplayName("should return empty when id does not belong to user")
        void shouldReturnEmptyForWrongUser() {
            Transaction tx = repository.save(
                    Transaction.create("Compra", 1000, Category.OTHER, testUser.getId()));

            var found = repository.findByIdAndUserId(tx.id(), UUID.randomUUID());

            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("should return empty when id does not exist")
        void shouldReturnEmptyForNonExistentId() {
            var found = repository.findByIdAndUserId(UUID.randomUUID(), testUser.getId());

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should remove transaction from database")
        void shouldRemoveTransaction() {
            Transaction tx = repository.save(
                    Transaction.create("Compra", 1000, Category.OTHER, testUser.getId()));

            repository.deleteById(tx.id());

            var found = repository.findByIdAndUserId(tx.id(), testUser.getId());
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findAllByUserId")
    class FindAllByUserId {

        @Test
        @DisplayName("should return only transactions of the given user")
        void shouldReturnUserTransactions() {
            Transaction tx = repository.save(
                    Transaction.create("Compra", 1000, Category.OTHER, testUser.getId()));

            TransactionPage result = repository.findAllByUserId(testUser.getId(), 0, 20);

            assertEquals(1, result.totalElements());
            assertEquals(tx.id(), result.content().get(0).id());
        }

        @Test
        @DisplayName("should return empty for user with no transactions")
        void shouldReturnEmptyForUserWithoutTransactions() {
            TransactionPage result = repository.findAllByUserId(testUser.getId(), 0, 20);

            assertTrue(result.content().isEmpty());
        }
    }

    @Nested
    @DisplayName("findAllByUserIdAndCategory")
    class FindAllByUserIdAndCategory {

        @Test
        @DisplayName("should filter by user and category")
        void shouldFilterByUserAndCategory() {
            repository.save(Transaction.create("Farmácia", 1500, Category.PHARMACY, testUser.getId()));
            repository.save(Transaction.create("Mercado", 5000, Category.SUPERMARKET, testUser.getId()));

            TransactionPage result = repository.findAllByUserIdAndCategory(testUser.getId(), Category.PHARMACY, 0, 20);

            assertEquals(1, result.totalElements());
            assertEquals(Category.PHARMACY, result.content().get(0).category());
        }

        @Test
        @DisplayName("should return empty when category does not match")
        void shouldReturnEmptyForNonMatchingCategory() {
            repository.save(Transaction.create("Mercado", 5000, Category.SUPERMARKET, testUser.getId()));

            TransactionPage result = repository.findAllByUserIdAndCategory(testUser.getId(), Category.AUTO, 0, 20);

            assertTrue(result.content().isEmpty());
        }

        @Test
        @DisplayName("should return all user transactions when category is null")
        void shouldReturnAllWhenCategoryNull() {
            repository.save(Transaction.create("Farmácia", 1500, Category.PHARMACY, testUser.getId()));
            repository.save(Transaction.create("Mercado", 5000, Category.SUPERMARKET, testUser.getId()));

            TransactionPage result = repository.findAllByUserIdAndCategory(testUser.getId(), null, 0, 20);

            assertEquals(2, result.totalElements());
        }
    }
}
