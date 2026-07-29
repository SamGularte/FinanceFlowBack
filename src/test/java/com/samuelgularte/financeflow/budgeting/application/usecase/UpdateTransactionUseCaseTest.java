package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.usecase.request.UpdateTransactionRequest;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.i18n.Messages;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTransactionUseCaseTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private Messages messages;

    private UpdateTransactionUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final UUID transactionId = UUID.randomUUID();
    private final LocalDateTime createdAt = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        useCase = new UpdateTransactionUseCase(repository, messages);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should update all fields when all are provided")
        void shouldUpdateAllFields() {
            var existing = new Transaction(transactionId, "Old", BigDecimal.valueOf(1000, 2), Category.OTHER, userId, createdAt);
            when(repository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(existing));

            var input = new UpdateTransactionRequest("New", BigDecimal.valueOf(2000, 2), Category.SUPERMARKET);
            var saved = new Transaction(transactionId, "New", BigDecimal.valueOf(2000, 2), Category.SUPERMARKET, userId, createdAt);
            when(repository.save(any())).thenReturn(saved);

            TransactionOutput result = useCase.execute(transactionId, userId, input);

            assertEquals("New", result.description());
            assertEquals("SUPERMARKET", result.category());
            assertThat(result.valor()).isEqualByComparingTo(BigDecimal.valueOf(2000, 2));
        }

        @Test
        @DisplayName("should keep existing fields when input fields are null")
        void shouldKeepExistingFieldsWhenNull() {
            var existing = new Transaction(transactionId, "Existing", BigDecimal.valueOf(5000, 2), Category.SUPERMARKET, userId, createdAt);
            when(repository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.of(existing));

            var input = new UpdateTransactionRequest(null, null, null);
            when(repository.save(any())).thenReturn(existing);

            TransactionOutput result = useCase.execute(transactionId, userId, input);

            assertEquals("Existing", result.description());
            assertEquals("SUPERMARKET", result.category());
            assertThat(result.valor()).isEqualByComparingTo(BigDecimal.valueOf(5000, 2));
        }

        @Test
        @DisplayName("should throw EntityNotFoundException when transaction not found")
        void shouldThrowWhenNotFound() {
            when(repository.findByIdAndUserId(transactionId, userId)).thenReturn(Optional.empty());

            var input = new UpdateTransactionRequest("New", BigDecimal.valueOf(1000, 2), Category.OTHER);

            assertThrows(EntityNotFoundException.class, () -> useCase.execute(transactionId, userId, input));
        }
    }
}
