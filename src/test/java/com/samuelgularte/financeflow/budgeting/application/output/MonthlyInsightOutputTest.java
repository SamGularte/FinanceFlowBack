package com.samuelgularte.financeflow.budgeting.application.output;

import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MonthlyInsightOutputTest {

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("should map all fields from domain to output")
        void shouldMapAllFields() {
            var insight = new MonthlyInsight(
                    UUID.randomUUID(), UUID.randomUUID(), 2026, 7,
                    "Insight muito útil",
                    LocalDateTime.of(2026, 7, 29, 10, 0, 0)
            );

            var output = MonthlyInsightOutput.from(insight);

            assertEquals("Insight muito útil", output.content());
            assertEquals(LocalDateTime.of(2026, 7, 29, 10, 0, 0), output.generatedAt());
        }
    }
}
