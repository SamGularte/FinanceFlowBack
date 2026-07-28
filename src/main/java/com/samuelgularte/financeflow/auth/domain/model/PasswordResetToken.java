package com.samuelgularte.financeflow.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetToken(
        UUID id,
        String token,
        UUID userId,
        Instant expiryDate
) {
    public static PasswordResetToken create(String token, Instant expiryDate, UUID userId) {
        return new PasswordResetToken(UUID.randomUUID(), token, userId, expiryDate);
    }
}
