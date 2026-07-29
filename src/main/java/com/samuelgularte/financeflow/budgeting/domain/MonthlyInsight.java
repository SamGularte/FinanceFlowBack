package com.samuelgularte.financeflow.budgeting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MonthlyInsight(
        UUID id,
        UUID userId,
        int year,
        int month,
        String content,
        LocalDateTime generatedAt
) {

    public boolean isClosedMonth(LocalDate now) {
        return year < now.getYear() || (year == now.getYear() && month < now.getMonthValue());
    }

    public boolean isLastDayOfMonth() {
        var generatedDate = generatedAt.toLocalDate();
        var lastDay = LocalDate.of(year, month, 1).lengthOfMonth();
        return generatedDate.getDayOfMonth() == lastDay;
    }

    public boolean wasGeneratedOnDate(LocalDate date) {
        return generatedAt.toLocalDate().equals(date);
    }
}
