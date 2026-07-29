package com.samuelgularte.financeflow.budgeting.application.output;

import com.samuelgularte.financeflow.budgeting.domain.Transaction;

import java.math.BigDecimal;

public record TransactionOutput(
        String id,
        String description,
        String category,
        BigDecimal valor,
        String createdAt
) {
    public static TransactionOutput from(Transaction transaction) {
        return new TransactionOutput(
                transaction.id().toString(),
                transaction.description(),
                transaction.category().name(),
                transaction.amount(),
                transaction.createdAt().toString()
        );
    }
}
