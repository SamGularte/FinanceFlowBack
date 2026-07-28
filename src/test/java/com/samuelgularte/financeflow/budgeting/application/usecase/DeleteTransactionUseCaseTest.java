package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteTransactionUseCaseTest {

    @Mock
    private TransactionRepository repository;

    private DeleteTransactionUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new DeleteTransactionUseCase(repository);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should delete transaction when it exists and belongs to user")
        void shouldDeleteWhenFound() {
            var transaction = new Transaction(transactionId, "Test", 1000, Category.OTHER, userId, LocalDateTime.now());
            when(repository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(transaction));

            useCase.execute(transactionId, userId);

            verify(repository).delete(transaction);
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when transaction not found")
        void shouldThrowWhenNotFound() {
            when(repository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> useCase.execute(transactionId, userId));
            verify(repository, never()).delete(any());
        }
    }
}
