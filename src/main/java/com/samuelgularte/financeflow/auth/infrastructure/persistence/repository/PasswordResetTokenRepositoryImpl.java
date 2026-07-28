package com.samuelgularte.financeflow.auth.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import com.samuelgularte.financeflow.auth.domain.repository.PasswordResetTokenRepository;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public PasswordResetTokenRepositoryImpl(PasswordResetTokenJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(PasswordResetTokenEntity::toDomain);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken resetToken) {
        UserEntity userEntity = entityManager.getReference(UserEntity.class, resetToken.userId());
        return jpaRepository.save(PasswordResetTokenEntity.from(resetToken, userEntity)).toDomain();
    }

    @Override
    public void delete(PasswordResetToken resetToken) {
        jpaRepository.deleteById(resetToken.id());
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
