package com.samuelgularte.financeflow.budgeting.application.tool;

import com.samuelgularte.financeflow.budgeting.application.input.PersistTransactionInput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PersistTransactionTool {

    private final TransactionRepository transactionRepository;

    public PersistTransactionTool(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Tool(name = "persist_transaction", description = "Persiste uma nova transacao financeira")
    public TransactionOutput execute(PersistTransactionInput input, ToolContext context) {
        UUID userId = UUID.fromString((String) context.getContext().get("userId"));
        LocalDateTime createdAt = input.createdAt() != null && !input.createdAt().isBlank()
                ? LocalDateTime.parse(input.createdAt())
                : LocalDateTime.now();
        Transaction transaction = transactionRepository.save(
                Transaction.create(input.description(), input.amount(), input.category(), userId, createdAt)
        );
        return TransactionOutput.from(transaction);
    }
}
