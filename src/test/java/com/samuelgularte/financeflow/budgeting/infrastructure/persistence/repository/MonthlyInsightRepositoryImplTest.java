package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(MonthlyInsightRepositoryImpl.class)
class MonthlyInsightRepositoryImplTest {

    @Autowired
    private MonthlyInsightRepositoryImpl repository;

    @Autowired
    private EntityManager entityManager;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity(UUID.randomUUID(), "testuser", "test@email.com", "password", null, null);
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist and return the insight")
        void shouldPersistAndReturn() {
            var insight = new MonthlyInsight(
                    UUID.randomUUID(), testUser.getId(), 2026, 7,
                    "Insight de teste",
                    LocalDateTime.now()
            );

            var saved = repository.save(insight);

            assertEquals(insight.id(), saved.id());
            assertEquals(insight.userId(), saved.userId());
            assertEquals(2026, saved.year());
            assertEquals(7, saved.month());
            assertEquals("Insight de teste", saved.content());
            assertNotNull(saved.generatedAt());
        }
    }

    @Nested
    @DisplayName("save updating existing")
    class SaveUpdate {

        @Test
        @DisplayName("should update content when saving with existing ID")
        void shouldUpdateExistingInsight() {
            var insight = repository.save(new MonthlyInsight(
                    UUID.randomUUID(), testUser.getId(), 2026, 7,
                    "Versão original",
                    LocalDateTime.now()
            ));

            var updated = new MonthlyInsight(
                    insight.id(), insight.userId(), 2026, 7,
                    "Versão atualizada",
                    LocalDateTime.now()
            );
            var result = repository.save(updated);

            assertEquals(insight.id(), result.id());
            assertEquals("Versão atualizada", result.content());
        }
    }

    @Nested
    @DisplayName("findByUserIdAndYearAndMonth")
    class FindByUserIdAndYearAndMonth {

        @Test
        @DisplayName("should return insight when exists")
        void shouldReturnInsightWhenExists() {
            var insight = repository.save(new MonthlyInsight(
                    UUID.randomUUID(), testUser.getId(), 2026, 7,
                    "Insight de julho",
                    LocalDateTime.now()
            ));

            var found = repository.findByUserIdAndYearAndMonth(testUser.getId(), 2026, 7);

            assertTrue(found.isPresent());
            assertEquals(insight.id(), found.get().id());
            assertEquals("Insight de julho", found.get().content());
        }

        @Test
        @DisplayName("should return empty when no insight for month")
        void shouldReturnEmptyWhenNotFound() {
            var found = repository.findByUserIdAndYearAndMonth(testUser.getId(), 2025, 1);

            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("should not return insight from other user")
        void shouldNotReturnInsightFromOtherUser() {
            repository.save(new MonthlyInsight(
                    UUID.randomUUID(), testUser.getId(), 2026, 7,
                    "Meu insight",
                    LocalDateTime.now()
            ));

            var otherUser = UUID.randomUUID();
            var found = repository.findByUserIdAndYearAndMonth(otherUser, 2026, 7);

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("unique constraint")
    class UniqueConstraint {

        @Test
        @DisplayName("should reject duplicate userId, year, month")
        void shouldRejectDuplicate() {
            repository.save(new MonthlyInsight(
                    UUID.randomUUID(), testUser.getId(), 2026, 7,
                    "Primeiro insight",
                    LocalDateTime.now()
            ));
            entityManager.flush();

            assertThrows(Exception.class, () -> {
                repository.save(new MonthlyInsight(
                        UUID.randomUUID(), testUser.getId(), 2026, 7,
                        "Insight duplicado",
                        LocalDateTime.now()
                ));
                entityManager.flush();
            });
        }
    }
}
