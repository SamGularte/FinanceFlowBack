package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity.TransactionEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final EntityManager entityManager;

    @Override
    public Transaction save(Transaction transaction) {
        User user = entityManager.getReference(User.class, transaction.userId());
        return jpaRepository.save(TransactionEntity.from(transaction, user)).toDomain();
    }

    @Override
    public Page<Transaction> findAllByCategory(Category category, Pageable pageable) {
        return jpaRepository.findAllByCategory(category, pageable)
                .map(TransactionEntity::toDomain);
    }

    @Override
    public Page<Transaction> findAllByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findAllByUserId(userId, pageable)
                .map(TransactionEntity::toDomain);
    }

    @Override
    public Page<Transaction> findAllByUserIdAndCategory(UUID userId, Category category, Pageable pageable) {
        return jpaRepository.findAllByUserIdAndCategory(userId, category, pageable)
                .map(TransactionEntity::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId)
                .map(TransactionEntity::toDomain);
    }

    @Override
    public void delete(Transaction transaction) {
        jpaRepository.deleteById(transaction.id());
    }
}
