package com.samuelgularte.financeflow.budgeting.domain.repository;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.TransactionPage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    TransactionPage findAllByCategory(Category category, int page, int size);

    TransactionPage findAllByUserId(UUID userId, int page, int size);

    TransactionPage findAllByUserIdAndCategory(UUID userId, Category category, int page, int size);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    void deleteById(UUID id);

    BigDecimal sumByUserIdAndMonth(UUID userId, int year, int month);

    Map<Category, BigDecimal> sumGroupByCategoryAndMonth(UUID userId, int year, int month);

    Map<Integer, BigDecimal> sumByDayAndMonth(UUID userId, int year, int month);

    List<Transaction> findTopByUserIdAndMonth(UUID userId, int year, int month, int limit);

    long countByUserIdAndMonth(UUID userId, int year, int month);
}
