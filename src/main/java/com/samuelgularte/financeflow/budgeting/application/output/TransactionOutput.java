package com.samuelgularte.financeflow.budgeting.application.output;

import com.samuelgularte.financeflow.budgeting.domain.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TransactionOutput(
        String id,
        String description,
        String category,
        double valor
) {
    public static TransactionOutput from(Transaction transaction) {
        return new TransactionOutput(
                transaction.id().toString(),
                transaction.description(),
                transaction.category().name(),
                BigDecimal.valueOf(transaction.amount(), 2).setScale(2, RoundingMode.HALF_UP).doubleValue()
        );
    }
}
