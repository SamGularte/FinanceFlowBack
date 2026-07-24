package com.samuelgularte.financeflow.auth.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import jakarta.persistence.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User createAndPersistUser(String userName, String email) {
        User user = new User(userName, email, "encoded-pass");
        return entityManager.persistFlushFind(user);
    }

    @Nested
    @DisplayName("findByUserName")
    class FindByUserName {

        @Test
        @DisplayName("should return user when username exists")
        void shouldReturnUserWhenExists() {
            createAndPersistUser("joao", "joao@email.com");

            Optional<User> found = userRepository.findByUserName("joao");

            assertTrue(found.isPresent());
            assertEquals("joao", found.get().getUserName());
            assertEquals("joao@email.com", found.get().getEmail());
        }

        @Test
        @DisplayName("should return empty when username does not exist")
        void shouldReturnEmptyWhenNotExists() {
            Optional<User> found = userRepository.findByUserName("naoexiste");

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should return user when email exists")
        void shouldReturnUserWhenExists() {
            createAndPersistUser("joao", "joao@email.com");

            Optional<User> found = userRepository.findByEmail("joao@email.com");

            assertTrue(found.isPresent());
            assertEquals("joao", found.get().getUserName());
            assertEquals("joao@email.com", found.get().getEmail());
        }

        @Test
        @DisplayName("should return empty when email does not exist")
        void shouldReturnEmptyWhenNotExists() {
            Optional<User> found = userRepository.findByEmail("naoexiste@email.com");

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("should return true when email exists")
        void shouldReturnTrueWhenExists() {
            createAndPersistUser("joao", "joao@email.com");

            assertTrue(userRepository.existsByEmail("joao@email.com"));
        }

        @Test
        @DisplayName("should return false when email does not exist")
        void shouldReturnFalseWhenNotExists() {
            assertFalse(userRepository.existsByEmail("naoexiste@email.com"));
        }
    }

    @Nested
    @DisplayName("existsByUserName")
    class ExistsByUserName {

        @Test
        @DisplayName("should return true when username exists")
        void shouldReturnTrueWhenExists() {
            createAndPersistUser("joao", "joao@email.com");

            assertTrue(userRepository.existsByUserName("joao"));
        }

        @Test
        @DisplayName("should return false when username does not exist")
        void shouldReturnFalseWhenNotExists() {
            assertFalse(userRepository.existsByUserName("naoexiste"));
        }
    }

    @Nested
    @DisplayName("Unique constraints")
    class UniqueConstraints {

        @Test
        @DisplayName("should throw PersistenceException when username is duplicated")
        void shouldThrowWhenUsernameDuplicated() {
            createAndPersistUser("joao", "joao@email.com");

            User duplicate = new User("joao", "outro@email.com", "pass");

            assertThrows(PersistenceException.class,
                    () -> entityManager.persistFlushFind(duplicate));
        }

        @Test
        @DisplayName("should throw PersistenceException when email is duplicated")
        void shouldThrowWhenEmailDuplicated() {
            createAndPersistUser("joao", "joao@email.com");

            User duplicate = new User("outro", "joao@email.com", "pass");

            assertThrows(PersistenceException.class,
                    () -> entityManager.persistFlushFind(duplicate));
        }
    }
}
