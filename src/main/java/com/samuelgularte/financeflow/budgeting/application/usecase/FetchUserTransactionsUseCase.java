package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.output.TransactionOutput;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FetchUserTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public FetchUserTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Page<TransactionOutput> execute(UUID userId, Category category, Pageable pageable) {
        if (category != null) {
            return transactionRepository.findAllByUserIdAndCategory(userId, category, pageable)
                    .map(TransactionOutput::from);
        }
        return transactionRepository.findAllByUserId(userId, pageable)
                .map(TransactionOutput::from);
    }
}
