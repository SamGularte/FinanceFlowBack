package com.samuelgularte.financeflow.auth.domain.repository;

import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import com.samuelgularte.financeflow.auth.domain.model.User;

import java.util.Optional;

public interface PasswordResetTokenRepository {

    Optional<PasswordResetToken> findByToken(String token);

    PasswordResetToken save(PasswordResetToken resetToken);

    void delete(PasswordResetToken resetToken);

    void deleteByUser(User user);
}
