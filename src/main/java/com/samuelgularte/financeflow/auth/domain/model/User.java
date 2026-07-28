package com.samuelgularte.financeflow.auth.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record User(
        UUID id,
        String userName,
        String email,
        String password,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
    public static User create(String userName, String email, String password) {
        var now = LocalDateTime.now();
        return new User(UUID.randomUUID(), userName, email, password, now, now);
    }
}
