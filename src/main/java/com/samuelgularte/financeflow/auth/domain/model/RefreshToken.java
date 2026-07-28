package com.samuelgularte.financeflow.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public record RefreshToken(
        UUID id,
        String token,
        UUID userId,
        Instant expiryDate
) {
    public static RefreshToken create(String token, Instant expiryDate, UUID userId) {
        return new RefreshToken(UUID.randomUUID(), token, userId, expiryDate);
    }
}
