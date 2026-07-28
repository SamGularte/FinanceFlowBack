package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {

    Page<TransactionEntity> findAllByCategory(Category category, Pageable pageable);

    @Query("SELECT t FROM TransactionEntity t WHERE t.user.id = :userId")
    Page<TransactionEntity> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM TransactionEntity t WHERE t.user.id = :userId AND (:category IS NULL OR t.category = :category)")
    Page<TransactionEntity> findAllByUserIdAndCategory(@Param("userId") UUID userId, @Param("category") Category category, Pageable pageable);

    @Query("SELECT t FROM TransactionEntity t WHERE t.id = :id AND t.user.id = :userId")
    Optional<TransactionEntity> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
