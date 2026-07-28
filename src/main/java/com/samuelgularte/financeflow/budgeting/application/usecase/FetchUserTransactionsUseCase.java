package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.TransactionPage;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class FetchUserTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public FetchUserTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionPage execute(UUID userId, Category category, int page, int size) {
        log.info("Fetching transactions for userId={}, category={}, page={}, size={}", userId, category, page, size);
        if (category != null) {
            return transactionRepository.findAllByUserIdAndCategory(userId, category, page, size);
        }
        return transactionRepository.findAllByUserId(userId, page, size);
    }
}
