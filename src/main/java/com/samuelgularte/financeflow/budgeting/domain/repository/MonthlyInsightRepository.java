package com.samuelgularte.financeflow.budgeting.domain.repository;

import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;

import java.util.Optional;
import java.util.UUID;

public interface MonthlyInsightRepository {

    Optional<MonthlyInsight> findByUserIdAndYearAndMonth(UUID userId, int year, int month);

    MonthlyInsight save(MonthlyInsight insight);
}
