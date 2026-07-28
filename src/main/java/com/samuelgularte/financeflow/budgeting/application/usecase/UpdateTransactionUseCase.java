package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.input.UpdateTransactionInput;
import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public UpdateTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionOutput execute(UUID id, UUID userId, UpdateTransactionInput input) {
        Transaction existing = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        String description = input.description() != null ? input.description() : existing.description();
        long amount = input.amount() != null ? input.amount() : existing.amount();
        var category = input.category() != null ? input.category() : existing.category();

        Transaction updated = new Transaction(existing.id(), description, amount, category, existing.userId(), existing.createdAt());
        return TransactionOutput.from(transactionRepository.save(updated));
    }
}
