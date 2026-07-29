package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    Page<TransactionEntity> findAllByCategory(Category category, Pageable pageable);

    Page<TransactionEntity> findAllByUserId(UUID userId, Pageable pageable);

    @Query("SELECT t FROM TransactionEntity t WHERE t.user.id = :userId AND (:category IS NULL OR t.category = :category)")
    Page<TransactionEntity> findAllByUserIdAndCategory(@Param("userId") UUID userId, @Param("category") Category category, Pageable pageable);

    Optional<TransactionEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TransactionEntity t WHERE t.user.id = :userId AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    BigDecimal sumByUserIdAndMonth(@Param("userId") UUID userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT t.category, SUM(t.amount) FROM TransactionEntity t WHERE t.user.id = :userId AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month GROUP BY t.category")
    List<Object[]> sumGroupByCategoryAndMonth(@Param("userId") UUID userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT DAY(t.createdAt), SUM(t.amount) FROM TransactionEntity t WHERE t.user.id = :userId AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month GROUP BY DAY(t.createdAt) ORDER BY DAY(t.createdAt)")
    List<Object[]> sumByDayAndMonth(@Param("userId") UUID userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT t FROM TransactionEntity t WHERE t.user.id = :userId AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month ORDER BY t.amount DESC")
    List<TransactionEntity> findTopByUserIdAndMonth(@Param("userId") UUID userId, @Param("year") int year, @Param("month") int month, Pageable pageable);

    @Query("SELECT COUNT(t) FROM TransactionEntity t WHERE t.user.id = :userId AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    long countByUserIdAndMonth(@Param("userId") UUID userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT t FROM TransactionEntity t WHERE t.user.id = :userId AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month ORDER BY t.createdAt DESC")
    List<TransactionEntity> findAllByUserIdAndMonth(@Param("userId") UUID userId, @Param("year") int year, @Param("month") int month);
}
