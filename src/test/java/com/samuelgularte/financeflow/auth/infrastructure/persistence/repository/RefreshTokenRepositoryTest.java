package com.samuelgularte.financeflow.auth.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.RefreshToken;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.User;
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
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User persistUser(String userName, String email) {
        return entityManager.persistFlushFind(new User(userName, email, "encoded-pass"));
    }

    private RefreshToken persistToken(String tokenValue, User user) {
        RefreshToken token = new RefreshToken(tokenValue, Instant.now().plus(7, ChronoUnit.DAYS), user);
        return entityManager.persistFlushFind(token);
    }

    @Nested
    @DisplayName("findByToken")
    class FindByToken {

        @Test
        @DisplayName("should return token when it exists")
        void shouldReturnTokenWhenExists() {
            User user = persistUser("joao", "joao@email.com");
            persistToken("token-123", user);

            Optional<RefreshToken> found = refreshTokenRepository.findByToken("token-123");

            assertTrue(found.isPresent());
            assertEquals("token-123", found.get().getToken());
            assertEquals(user.getId(), found.get().getUser().getId());
        }

        @Test
        @DisplayName("should return empty when token does not exist")
        void shouldReturnEmptyWhenNotExists() {
            Optional<RefreshToken> found = refreshTokenRepository.findByToken("naoexiste");

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteByToken")
    class DeleteByToken {

        @Test
        @DisplayName("should delete the specific token")
        void shouldDeleteSpecificToken() {
            User user = persistUser("joao", "joao@email.com");
            persistToken("token-1", user);
            persistToken("token-2", user);

            refreshTokenRepository.deleteByToken("token-1");

            assertTrue(refreshTokenRepository.findByToken("token-1").isEmpty());
            assertTrue(refreshTokenRepository.findByToken("token-2").isPresent());
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

            refreshTokenRepository.deleteByUser(user);

            assertTrue(refreshTokenRepository.findByToken("token-1").isEmpty());
            assertTrue(refreshTokenRepository.findByToken("token-2").isEmpty());
        }

        @Test
        @DisplayName("should not delete tokens from other users")
        void shouldNotDeleteTokensFromOtherUsers() {
            User userA = persistUser("joao", "joao@email.com");
            User userB = persistUser("maria", "maria@email.com");
            persistToken("token-a", userA);
            persistToken("token-b", userB);

            refreshTokenRepository.deleteByUser(userA);

            assertTrue(refreshTokenRepository.findByToken("token-a").isEmpty());
            assertTrue(refreshTokenRepository.findByToken("token-b").isPresent());
        }
    }
}
