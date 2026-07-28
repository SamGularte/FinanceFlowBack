package com.samuelgularte.financeflow.auth.domain.repository;

import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {

    Optional<PasswordResetToken> findByToken(String token);

    PasswordResetToken save(PasswordResetToken resetToken);

    void delete(PasswordResetToken resetToken);

    void deleteByUserId(UUID userId);
}
