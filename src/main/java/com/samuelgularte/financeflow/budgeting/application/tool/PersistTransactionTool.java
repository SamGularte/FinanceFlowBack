package com.samuelgularte.financeflow.budgeting.application.tool;

import com.samuelgularte.financeflow.budgeting.application.usecase.request.PersistTransactionRequest;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.i18n.Messages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PersistTransactionTool {

    private final TransactionRepository transactionRepository;
    private final Messages messages;

    public PersistTransactionTool(TransactionRepository transactionRepository, Messages messages) {
        this.transactionRepository = transactionRepository;
        this.messages = messages;
    }

    @Tool(name = "persist_transaction", description = "Persiste uma nova transacao financeira")
    public TransactionOutput execute(PersistTransactionRequest input, ToolContext context) {
        String userIdStr = (String) context.getContext().get("userId");
        if (userIdStr == null) {
            throw new IllegalArgumentException(messages.get("error.tool.user-id-required"));
        }
        UUID userId = UUID.fromString(userIdStr);
        LocalDateTime createdAt = input.createdAt() != null && !input.createdAt().isBlank()
                ? LocalDateTime.parse(input.createdAt())
                : LocalDateTime.now();
        if (input.amount() < 0) {
            throw new IllegalArgumentException(messages.get("error.tool.amount-negative"));
        }
        log.info("Persisting transaction: description={}, amount={}, category={}, userId={}", input.description(), input.amount(), input.category(), userId);
        Transaction transaction = transactionRepository.save(
                Transaction.create(input.description(), input.amount(), input.category(), userId, createdAt)
        );
        return TransactionOutput.from(transaction);
    }
}
