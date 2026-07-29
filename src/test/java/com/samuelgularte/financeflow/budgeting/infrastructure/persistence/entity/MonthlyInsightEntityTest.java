package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyInsightEntityTest {

    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("should map all fields from domain to entity")
        void shouldMapAllFields() {
            var insight = new MonthlyInsight(
                    UUID.randomUUID(), userId, 2026, 7,
                    "Insight de teste",
                    LocalDateTime.of(2026, 7, 29, 10, 0, 0)
            );
            var user = new UserEntity();
            user.setId(userId);

            var entity = MonthlyInsightEntity.from(insight, user);

            assertEquals(insight.id(), entity.getId());
            assertEquals(userId, entity.getUser().getId());
            assertEquals(2026, entity.getYear());
            assertEquals(7, entity.getMonth());
            assertEquals("Insight de teste", entity.getContent());
            assertEquals(LocalDateTime.of(2026, 7, 29, 10, 0, 0), entity.getGeneratedAt());
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("should map all fields from entity to domain")
        void shouldMapAllFields() {
            var user = new UserEntity();
            user.setId(userId);
            UUID id = UUID.randomUUID();
            var entity = new MonthlyInsightEntity(
                    id, user, 2026, 7,
                    "Insight de teste",
                    LocalDateTime.of(2026, 7, 29, 10, 0, 0)
            );

            var insight = entity.toDomain();

            assertEquals(id, insight.id());
            assertEquals(userId, insight.userId());
            assertEquals(2026, insight.year());
            assertEquals(7, insight.month());
            assertEquals("Insight de teste", insight.content());
            assertEquals(LocalDateTime.of(2026, 7, 29, 10, 0, 0), insight.generatedAt());
        }
    }

    @Nested
    @DisplayName("roundtrip")
    class Roundtrip {

        @Test
        @DisplayName("should preserve all fields after from then toDomain")
        void shouldPreserveFields() {
            var original = new MonthlyInsight(
                    UUID.randomUUID(), userId, 2026, 7,
                    "Insight de teste",
                    LocalDateTime.of(2026, 7, 29, 10, 0, 0)
            );
            var user = new UserEntity();
            user.setId(userId);

            var entity = MonthlyInsightEntity.from(original, user);
            var result = entity.toDomain();

            assertEquals(original, result);
        }
    }
}
