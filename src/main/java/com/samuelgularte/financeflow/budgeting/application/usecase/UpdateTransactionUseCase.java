package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.usecase.request.UpdateTransactionRequest;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.i18n.Messages;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final Messages messages;

    public UpdateTransactionUseCase(TransactionRepository transactionRepository, Messages messages) {
        this.transactionRepository = transactionRepository;
        this.messages = messages;
    }

    @Transactional
    public TransactionOutput execute(UUID id, UUID userId, UpdateTransactionRequest input) {
        log.info("Updating transaction id={}, userId={}", id, userId);
        Transaction existing = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException(messages.get("error.transaction.not-found")));

        String description = input.description() != null ? input.description() : existing.description();
        long amount = input.amount() != null ? input.amount() : existing.amount();
        var category = input.category() != null ? input.category() : existing.category();

        Transaction updated = new Transaction(existing.id(), description, amount, category, existing.userId(), existing.createdAt());
        return TransactionOutput.from(transactionRepository.save(updated));
    }
}
