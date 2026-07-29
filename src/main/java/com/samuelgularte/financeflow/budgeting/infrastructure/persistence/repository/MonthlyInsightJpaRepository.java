package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity.MonthlyInsightEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MonthlyInsightJpaRepository extends JpaRepository<MonthlyInsightEntity, UUID> {

    Optional<MonthlyInsightEntity> findByUserIdAndYearAndMonth(UUID userId, int year, int month);
}
