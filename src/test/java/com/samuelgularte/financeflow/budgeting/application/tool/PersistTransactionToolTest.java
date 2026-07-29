package com.samuelgularte.financeflow.budgeting.application.tool;

import com.samuelgularte.financeflow.budgeting.application.usecase.request.PersistTransactionRequest;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.i18n.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersistTransactionToolTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private Messages messages;

    private PersistTransactionTool tool;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final ToolContext toolContext = new ToolContext(Map.of("userId", userId.toString()));

    @BeforeEach
    void setUp() {
        tool = new PersistTransactionTool(transactionRepository, messages);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should extract userId from ToolContext and pass to Transaction.create")
        void shouldExtractUserIdFromContext() {
            var input = new PersistTransactionRequest("Compra mercado", BigDecimal.valueOf(5000, 2), Category.SUPERMARKET, null);
            when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            tool.execute(input, toolContext);

            verify(transactionRepository).save(transactionCaptor.capture());
            Transaction saved = transactionCaptor.getValue();
            assertEquals(userId, saved.userId());
            assertEquals("Compra mercado", saved.description());
            assertTrue(BigDecimal.valueOf(5000, 2).compareTo(saved.amount()) == 0);
            assertEquals(Category.SUPERMARKET, saved.category());
            assertNotNull(saved.createdAt());
        }

        @Test
        @DisplayName("should use provided createdAt when informed")
        void shouldUseProvidedCreatedAt() {
            var input = new PersistTransactionRequest("Compra", BigDecimal.valueOf(1000, 2), Category.OTHER, "2026-07-26T15:30:00");
            when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            tool.execute(input, toolContext);

            verify(transactionRepository).save(transactionCaptor.capture());
            assertEquals(LocalDateTime.of(2026, 7, 26, 15, 30, 0), transactionCaptor.getValue().createdAt());
        }

        @Test
        @DisplayName("should throw when ToolContext has no userId")
        void shouldThrowWhenUserIdMissing() {
            var input = new PersistTransactionRequest("Compra", BigDecimal.valueOf(1000, 2), Category.OTHER, null);
            var emptyContext = new ToolContext(Map.of());

            assertThrows(IllegalArgumentException.class, () -> tool.execute(input, emptyContext));
        }

        @Test
        @DisplayName("should return TransactionOutput with correct fields")
        void shouldReturnTransactionOutput() {
            var input = new PersistTransactionRequest("Farmácia", BigDecimal.valueOf(1500, 2), Category.PHARMACY, null);
            when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            TransactionOutput output = tool.execute(input, toolContext);

            assertNotNull(output.id());
            assertEquals("Farmácia", output.description());
            assertEquals("PHARMACY", output.category());
            assertEquals(BigDecimal.valueOf(1500, 2), output.valor());
            assertNotNull(output.createdAt());
        }
    }
}
