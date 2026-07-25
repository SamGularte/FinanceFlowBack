package com.samuelgularte.financeflow.auth.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.PasswordResetTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetToken, UUID>, PasswordResetTokenRepository {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);
}
