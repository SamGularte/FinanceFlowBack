package com.samuelgularte.financeflow.auth.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.domain.model.RefreshToken;
import com.samuelgularte.financeflow.auth.domain.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final EntityManager entityManager;

    public RefreshTokenRepositoryImpl(RefreshTokenJpaRepository jpaRepository, EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(RefreshTokenEntity::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        UserEntity userEntity = entityManager.getReference(UserEntity.class, refreshToken.userId());
        return jpaRepository.save(RefreshTokenEntity.from(refreshToken, userEntity)).toDomain();
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        jpaRepository.deleteById(refreshToken.id());
    }

    @Override
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
