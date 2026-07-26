package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TransactionRepositoryImpl.class)
class vTransactionRepositoryImplTest {

    @Autowired
    private TransactionRepositoryImpl repository;

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist and return the transaction")
        void shouldPersistAndReturn() {
            Transaction transaction = Transaction.create("Compra mercado", 5000, Category.SUPERMARKET);

            Transaction saved = repository.save(transaction);

            assertNotNull(saved.id());
            assertEquals(transaction.description(), saved.description());
            assertEquals(transaction.amount(), saved.amount());
            assertEquals(transaction.category(), saved.category());
        }
    }

    @Nested
    @DisplayName("save updating existing")
    class SaveUpdate {

        @Test
        @DisplayName("should update fields when saving with existing ID")
        void shouldUpdateExistingTransaction() {
            Transaction transaction = repository.save(Transaction.create("Compra mercado", 5000, Category.SUPERMARKET));

            Transaction updated = new Transaction(
                    transaction.id(),
                    "Compra mercado atualizada",
                    6000,
                    Category.SUPERMARKET
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
            Transaction mercado = repository.save(Transaction.create("Compra mercado", 5000, Category.SUPERMARKET));
            Transaction remedio = repository.save(Transaction.create("Farmácia", 1500, Category.PHARMACY));

            List<Transaction> result = repository.findAllByCategory(Category.PHARMACY);

            assertEquals(1, result.size());
            assertEquals(remedio.id(), result.get(0).id());
        }

        @Test
        @DisplayName("should return empty list when no transaction matches the category")
        void shouldReturnEmptyWhenNoMatch() {
            repository.save(Transaction.create("Compra mercado", 5000, Category.SUPERMARKET));

            List<Transaction> result = repository.findAllByCategory(Category.AUTO);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return multiple transactions of the same category")
        void shouldReturnMultipleMatchingTransactions() {
            repository.save(Transaction.create("Farmácia 1", 1000, Category.PHARMACY));
            repository.save(Transaction.create("Farmácia 2", 2000, Category.PHARMACY));
            repository.save(Transaction.create("Mercado", 5000, Category.SUPERMARKET));

            List<Transaction> result = repository.findAllByCategory(Category.PHARMACY);

            assertEquals(2, result.size());
        }
    }
}
