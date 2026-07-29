package com.samuelgularte.financeflow.auth.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
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
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenJpaRepository passwordResetTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UserEntity persistUser(String username, String email) {
        return entityManager.persistFlushFind(new UserEntity(UUID.randomUUID(), username, email, "encoded-pass", null, null));
    }

    private PasswordResetTokenEntity persistToken(String tokenValue, UserEntity user) {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity(UUID.randomUUID(), tokenValue, Instant.now().plus(24, ChronoUnit.HOURS), user);
        return entityManager.persistFlushFind(token);
    }

    @Nested
    @DisplayName("findByToken")
    class FindByToken {

        @Test
        @DisplayName("should return token when it exists")
        void shouldReturnTokenWhenExists() {
            UserEntity user = persistUser("joao", "joao@email.com");
            persistToken("reset-token-123", user);

            Optional<PasswordResetTokenEntity> found = passwordResetTokenRepository.findByToken("reset-token-123");

            assertTrue(found.isPresent());
            assertEquals("reset-token-123", found.get().getToken());
            assertEquals(user.getId(), found.get().getUser().getId());
        }

        @Test
        @DisplayName("should return empty when token does not exist")
        void shouldReturnEmptyWhenNotExists() {
            Optional<PasswordResetTokenEntity> found = passwordResetTokenRepository.findByToken("naoexiste");

            assertTrue(found.isEmpty());
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

            passwordResetTokenRepository.deleteByUserId(user.getId());
            entityManager.flush();
            entityManager.clear();

            Optional<PasswordResetTokenEntity> found1 = passwordResetTokenRepository.findByToken("token-1");
            Optional<PasswordResetTokenEntity> found2 = passwordResetTokenRepository.findByToken("token-2");
            assertTrue(found1.isEmpty());
            assertTrue(found2.isEmpty());
        }

        @Test
        @DisplayName("should not delete tokens from other users")
        void shouldNotDeleteTokensFromOtherUsers() {
            UserEntity userA = persistUser("joao", "joao@email.com");
            UserEntity userB = persistUser("maria", "maria@email.com");
            persistToken("token-a", userA);
            persistToken("token-b", userB);

            passwordResetTokenRepository.deleteByUserId(userA.getId());
            entityManager.flush();
            entityManager.clear();

            assertTrue(passwordResetTokenRepository.findByToken("token-a").isEmpty());
            assertTrue(passwordResetTokenRepository.findByToken("token-b").isPresent());
        }
    }
}
