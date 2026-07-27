package com.samuelgularte.financeflow.budgeting.domain;

import java.util.UUID;

public record Transaction(
        UUID id,
        String description,
        long amount,
        Category category,
        UUID userId
) {

    public static Transaction create(String description, long amount, Category category, UUID userId) {
        return new Transaction(UUID.randomUUID(), description, amount, category, userId);
    }
}
