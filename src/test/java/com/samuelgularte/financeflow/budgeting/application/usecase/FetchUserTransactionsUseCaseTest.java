package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FetchUserTransactionsUseCaseTest {

    @Mock
    private TransactionRepository repository;

    private FetchUserTransactionsUseCase useCase;
    private final UUID userId = UUID.randomUUID();
    private final Pageable pageable = PageRequest.of(0, 20);

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
            var page = new PageImpl<>(List.of(tx));
            when(repository.findAllByUserId(userId, pageable)).thenReturn(page);

            Page<TransactionOutput> result = useCase.execute(userId, null, pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("Compra", result.getContent().getFirst().description());
            verify(repository).findAllByUserId(userId, pageable);
            verify(repository, never()).findAllByUserIdAndCategory(any(), any(), any());
        }

        @Test
        @DisplayName("should filter by category when provided")
        void shouldFilterByCategory() {
            var tx = Transaction.create("Farmácia", 1500, Category.PHARMACY, userId, LocalDateTime.now());
            var page = new PageImpl<>(List.of(tx));
            when(repository.findAllByUserIdAndCategory(userId, Category.PHARMACY, pageable)).thenReturn(page);

            Page<TransactionOutput> result = useCase.execute(userId, Category.PHARMACY, pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("PHARMACY", result.getContent().getFirst().category());
            verify(repository).findAllByUserIdAndCategory(userId, Category.PHARMACY, pageable);
            verify(repository, never()).findAllByUserId(any(), any());
        }
    }
}
