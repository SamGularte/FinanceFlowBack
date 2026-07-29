package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;
import com.samuelgularte.financeflow.budgeting.domain.repository.MonthlyInsightRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity.MonthlyInsightEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MonthlyInsightRepositoryImpl implements MonthlyInsightRepository {

    private final MonthlyInsightJpaRepository jpaRepository;
    private final EntityManager entityManager;

    @Override
    public Optional<MonthlyInsight> findByUserIdAndYearAndMonth(UUID userId, int year, int month) {
        log.debug("Finding insight for userId={}, year={}, month={}", userId, year, month);
        return jpaRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .map(MonthlyInsightEntity::toDomain);
    }

    @Override
    public MonthlyInsight save(MonthlyInsight insight) {
        log.info("Saving insight for userId={}, year={}, month={}", insight.userId(), insight.year(), insight.month());
        UserEntity user = entityManager.getReference(UserEntity.class, insight.userId());
        return jpaRepository.save(MonthlyInsightEntity.from(insight, user)).toDomain();
    }
}
