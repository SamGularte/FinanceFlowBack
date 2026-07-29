package com.samuelgularte.financeflow.budgeting.application.output;

import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;

import java.time.LocalDateTime;

public record MonthlyInsightOutput(String content, LocalDateTime generatedAt) {

    public static MonthlyInsightOutput from(MonthlyInsight insight) {
        return new MonthlyInsightOutput(insight.content(), insight.generatedAt());
    }
}
