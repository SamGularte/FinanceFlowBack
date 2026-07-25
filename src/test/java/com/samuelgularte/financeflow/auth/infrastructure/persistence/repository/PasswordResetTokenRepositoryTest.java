package com.samuelgularte.financeflow.auth.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private PasswordResetTokenJpaRepository passwordResetTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User persistUser(String userName, String email) {
        return entityManager.persistFlushFind(new User(userName, email, "encoded-pass"));
    }

    private PasswordResetToken persistToken(String tokenValue, User user) {
        PasswordResetToken token = new PasswordResetToken(tokenValue, Instant.now().plus(24, ChronoUnit.HOURS), user);
        return entityManager.persistFlushFind(token);
    }

    @Nested
    @DisplayName("findByToken")
    class FindByToken {

        @Test
        @DisplayName("should return token when it exists")
        void shouldReturnTokenWhenExists() {
            User user = persistUser("joao", "joao@email.com");
            persistToken("reset-token-123", user);

            Optional<PasswordResetToken> found = passwordResetTokenRepository.findByToken("reset-token-123");

            assertTrue(found.isPresent());
            assertEquals("reset-token-123", found.get().getToken());
            assertEquals(user.getId(), found.get().getUser().getId());
        }

        @Test
        @DisplayName("should return empty when token does not exist")
        void shouldReturnEmptyWhenNotExists() {
            Optional<PasswordResetToken> found = passwordResetTokenRepository.findByToken("naoexiste");

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteByUser")
    class DeleteByUser {

        @Test
        @DisplayName("should delete all tokens from the given user")
        void shouldDeleteAllTokensFromUser() {
            User user = persistUser("joao", "joao@email.com");
            persistToken("token-1", user);
            persistToken("token-2", user);

            passwordResetTokenRepository.deleteByUser(user);

            Optional<PasswordResetToken> found1 = passwordResetTokenRepository.findByToken("token-1");
            Optional<PasswordResetToken> found2 = passwordResetTokenRepository.findByToken("token-2");
            assertTrue(found1.isEmpty());
            assertTrue(found2.isEmpty());
        }

        @Test
        @DisplayName("should not delete tokens from other users")
        void shouldNotDeleteTokensFromOtherUsers() {
            User userA = persistUser("joao", "joao@email.com");
            User userB = persistUser("maria", "maria@email.com");
            persistToken("token-a", userA);
            persistToken("token-b", userB);

            passwordResetTokenRepository.deleteByUser(userA);

            assertTrue(passwordResetTokenRepository.findByToken("token-a").isEmpty());
            assertTrue(passwordResetTokenRepository.findByToken("token-b").isPresent());
        }
    }
}
