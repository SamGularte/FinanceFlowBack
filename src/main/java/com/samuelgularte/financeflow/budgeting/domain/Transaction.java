package com.samuelgularte.financeflow.budgeting.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record Transaction(
        UUID id,
        String description,
        BigDecimal amount,
        Category category,
        UUID userId,
        LocalDateTime createdAt
) {

    public static Transaction create(String description, BigDecimal amount, Category category, UUID userId) {
        return new Transaction(UUID.randomUUID(), description, amount, category, userId, LocalDateTime.now());
    }

    public static Transaction create(String description, BigDecimal amount, Category category, UUID userId, LocalDateTime createdAt) {
        return new Transaction(UUID.randomUUID(), description, amount, category, userId, createdAt);
    }
}
