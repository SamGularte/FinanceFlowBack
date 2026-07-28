package com.samuelgularte.financeflow.budgeting.domain.repository;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.TransactionPage;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    TransactionPage findAllByCategory(Category category, int page, int size);

    TransactionPage findAllByUserId(UUID userId, int page, int size);

    TransactionPage findAllByUserIdAndCategory(UUID userId, Category category, int page, int size);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    void deleteById(UUID id);
}
