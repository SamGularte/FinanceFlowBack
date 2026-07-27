package com.samuelgularte.financeflow.budgeting.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
        UUID id,
        String description,
        long amount,
        Category category,
        UUID userId,
        LocalDateTime createdAt
) {

    public static Transaction create(String description, long amount, Category category, UUID userId) {
        return new Transaction(UUID.randomUUID(), description, amount, category, userId, LocalDateTime.now());
    }

    public static Transaction create(String description, long amount, Category category, UUID userId, LocalDateTime createdAt) {
        return new Transaction(UUID.randomUUID(), description, amount, category, userId, createdAt);
    }
}
