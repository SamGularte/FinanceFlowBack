package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.i18n.Messages;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final Messages messages;

    public DeleteTransactionUseCase(TransactionRepository transactionRepository, Messages messages) {
        this.transactionRepository = transactionRepository;
        this.messages = messages;
    }

    @Transactional
    public void execute(UUID id, UUID userId) {
        log.info("Deleting transaction id={}, userId={}", id, userId);
        var transaction = transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException(messages.get("error.transaction.not-found")));
        transactionRepository.deleteById(transaction.id());
    }
}
