package com.samuelgularte.financeflow.budgeting.domain;

import java.util.UUID;

public record Transaction(
        UUID id,
        String description,
        long amount,
        Category category
) {

    public static Transaction create(String description, long amount, Category category) {
        return new Transaction(UUID.randomUUID(), description, amount, category);
    }
}
