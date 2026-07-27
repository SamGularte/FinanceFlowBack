package com.samuelgularte.financeflow.budgeting.application.tool;

import com.samuelgularte.financeflow.budgeting.application.input.PersistTransactionInput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
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

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersistTransactionToolTest {

    @Mock
    private TransactionRepository transactionRepository;

    private PersistTransactionTool tool;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final ToolContext toolContext = new ToolContext(Map.of("userId", userId.toString()));

    @BeforeEach
    void setUp() {
        tool = new PersistTransactionTool(transactionRepository);
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should extract userId from ToolContext and pass to Transaction.create")
        void shouldExtractUserIdFromContext() {
            var input = new PersistTransactionInput("Compra mercado", 5000, Category.SUPERMARKET);
            when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            tool.execute(input, toolContext);

            verify(transactionRepository).save(transactionCaptor.capture());
            Transaction saved = transactionCaptor.getValue();
            assertEquals(userId, saved.userId());
            assertEquals("Compra mercado", saved.description());
            assertEquals(5000, saved.amount());
            assertEquals(Category.SUPERMARKET, saved.category());
        }

        @Test
        @DisplayName("should throw when ToolContext has no userId")
        void shouldThrowWhenUserIdMissing() {
            var input = new PersistTransactionInput("Compra", 1000, Category.OTHER);
            var emptyContext = new ToolContext(Map.of());

            assertThrows(NullPointerException.class, () -> tool.execute(input, emptyContext));
        }

        @Test
        @DisplayName("should return TransactionOutput with correct fields")
        void shouldReturnTransactionOutput() {
            var input = new PersistTransactionInput("Farmácia", 1500, Category.PHARMACY);
            when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            TransactionOutput output = tool.execute(input, toolContext);

            assertNotNull(output.id());
            assertEquals("Farmácia", output.description());
            assertEquals("PHARMACY", output.category());
            assertEquals(15.0, output.valor(), 0.001);
        }
    }
}
