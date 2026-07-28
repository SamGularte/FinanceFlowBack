package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.TransactionPage;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FetchUserTransactionsUseCaseTest {

    @Mock
    private TransactionRepository repository;

    private FetchUserTransactionsUseCase useCase;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new FetchUserTransactionsUseCase(repository);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should return all user transactions when category is null")
        void shouldReturnAllWhenCategoryNull() {
            var tx = Transaction.create("Compra", 1000, Category.OTHER, userId, LocalDateTime.now());
            var txPage = new TransactionPage(List.of(tx), 1, 0, 20);
            when(repository.findAllByUserId(userId, 0, 20)).thenReturn(txPage);

            TransactionPage result = useCase.execute(userId, null, 0, 20);

            assertEquals(1, result.totalElements());
            assertEquals("Compra", result.content().getFirst().description());
            verify(repository).findAllByUserId(userId, 0, 20);
            verify(repository, never()).findAllByUserIdAndCategory(any(), any(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("should filter by category when provided")
        void shouldFilterByCategory() {
            var tx = Transaction.create("Farmácia", 1500, Category.PHARMACY, userId, LocalDateTime.now());
            var txPage = new TransactionPage(List.of(tx), 1, 0, 20);
            when(repository.findAllByUserIdAndCategory(userId, Category.PHARMACY, 0, 20)).thenReturn(txPage);

            TransactionPage result = useCase.execute(userId, Category.PHARMACY, 0, 20);

            assertEquals(1, result.totalElements());
            assertEquals("PHARMACY", result.content().getFirst().category().name());
            verify(repository).findAllByUserIdAndCategory(userId, Category.PHARMACY, 0, 20);
            verify(repository, never()).findAllByUserId(any(), anyInt(), anyInt());
        }
    }
}
