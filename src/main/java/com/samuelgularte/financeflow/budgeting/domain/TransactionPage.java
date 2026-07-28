package com.samuelgularte.financeflow.budgeting.domain;

import java.util.List;

public record TransactionPage(
        List<Transaction> content,
        long totalElements,
        int page,
        int size
) {
    public long totalPages() {
        return size > 0 ? (totalElements + size - 1) / size : 0;
    }
}
