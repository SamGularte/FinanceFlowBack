package com.samuelgularte.financeflow.auth.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.RefreshTokenEntity;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import com.samuelgularte.financeflow.auth.domain.service.TokenHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenJpaRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity persistUser(String username, String email) {
        return entityManager.persistFlushFind(new UserEntity(UUID.randomUUID(), username, email, "encoded-pass", null, null));
    }

    private RefreshTokenEntity persistToken(String rawTokenValue, UserEntity user) {
        RefreshTokenEntity token = new RefreshTokenEntity(UUID.randomUUID(), TokenHasher.hash(rawTokenValue), user, Instant.now().plus(7, ChronoUnit.DAYS));
        return entityManager.persistFlushFind(token);
    }

    private String lookupHash(String rawTokenValue) {
        return TokenHasher.hash(rawTokenValue);
    }

    @Nested
    @DisplayName("findByToken")
    class FindByToken {

        @Test
        @DisplayName("should return token when it exists")
        void shouldReturnTokenWhenExists() {
            UserEntity user = persistUser("joao", "joao@email.com");
            persistToken("token-123", user);

            Optional<RefreshTokenEntity> found = refreshTokenRepository.findByToken(lookupHash("token-123"));

            assertTrue(found.isPresent());
            assertEquals(lookupHash("token-123"), found.get().getToken());
            assertEquals(user.getId(), found.get().getUser().getId());
        }

        @Test
        @DisplayName("should return empty when token does not exist")
        void shouldReturnEmptyWhenNotExists() {
            Optional<RefreshTokenEntity> found = refreshTokenRepository.findByToken(lookupHash("naoexiste"));

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("delete by entity")
    class DeleteByEntity {

        @Test
        @DisplayName("should delete the specific token")
        void shouldDeleteSpecificToken() {
            UserEntity user = persistUser("joao", "joao@email.com");
            persistToken("token-1", user);
            persistToken("token-2", user);

            refreshTokenRepository.findByToken(lookupHash("token-1"))
                    .ifPresent(entityManager::remove);

            assertTrue(refreshTokenRepository.findByToken(lookupHash("token-1")).isEmpty());
            assertTrue(refreshTokenRepository.findByToken(lookupHash("token-2")).isPresent());
        }
    }

    @Nested
    @DisplayName("deleteByUserId")
    class DeleteByUserId {

        @Test
        @DisplayName("should delete all tokens from the given user")
        void shouldDeleteAllTokensFromUser() {
            UserEntity user = persistUser("joao", "joao@email.com");
            persistToken("token-1", user);
            persistToken("token-2", user);

            refreshTokenRepository.deleteByUserId(user.getId());
            entityManager.flush();
            entityManager.clear();

            assertTrue(refreshTokenRepository.findByToken(lookupHash("token-1")).isEmpty());
            assertTrue(refreshTokenRepository.findByToken(lookupHash("token-2")).isEmpty());
        }

        @Test
        @DisplayName("should not delete tokens from other users")
        void shouldNotDeleteTokensFromOtherUsers() {
            UserEntity userA = persistUser("joao", "joao@email.com");
            UserEntity userB = persistUser("maria", "maria@email.com");
            persistToken("token-a", userA);
            persistToken("token-b", userB);

            refreshTokenRepository.deleteByUserId(userA.getId());
            entityManager.flush();
            entityManager.clear();

            assertTrue(refreshTokenRepository.findByToken(lookupHash("token-a")).isEmpty());
            assertTrue(refreshTokenRepository.findByToken(lookupHash("token-b")).isPresent());
        }
    }
}
