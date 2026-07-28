package com.samuelgularte.financeflow.budgeting.domain.repository;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    Page<Transaction> findAllByCategory(Category category, Pageable pageable);

    Page<Transaction> findAllByUserId(UUID userId, Pageable pageable);

    Page<Transaction> findAllByUserIdAndCategory(UUID userId, Category category, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    void delete(Transaction transaction);
}
