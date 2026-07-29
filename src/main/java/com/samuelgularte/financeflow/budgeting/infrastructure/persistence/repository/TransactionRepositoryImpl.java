package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.TransactionPage;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity.TransactionEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;
    private final EntityManager entityManager;

    @Override
    public Transaction save(Transaction transaction) {
        log.info("Saving transaction: id={}, description={}, amount={}", transaction.id(), transaction.description(), transaction.amount());
        UserEntity user = entityManager.getReference(UserEntity.class, transaction.userId());
        return jpaRepository.save(TransactionEntity.from(transaction, user)).toDomain();
    }

    @Override
    public TransactionPage findAllByCategory(Category category, int page, int size) {
        Page<TransactionEntity> entities = jpaRepository.findAllByCategory(category, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return toTransactionPage(entities, page, size);
    }

    @Override
    public TransactionPage findAllByUserId(UUID userId, int page, int size) {
        Page<TransactionEntity> entities = jpaRepository.findAllByUserId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return toTransactionPage(entities, page, size);
    }

    @Override
    public TransactionPage findAllByUserIdAndCategory(UUID userId, Category category, int page, int size) {
        Page<TransactionEntity> entities = jpaRepository.findAllByUserIdAndCategory(userId, category, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return toTransactionPage(entities, page, size);
    }

    @Override
    public Optional<Transaction> findByIdAndUserId(UUID id, UUID userId) {
        log.debug("Finding transaction by id={} and userId={}", id, userId);
        return jpaRepository.findByIdAndUserId(id, userId)
                .map(TransactionEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        log.info("Deleting transaction: id={}", id);
        jpaRepository.deleteById(id);
    }

    @Override
    public BigDecimal sumByUserIdAndMonth(UUID userId, int year, int month) {
        log.debug("Summing transactions for userId={}, year={}, month={}", userId, year, month);
        return jpaRepository.sumByUserIdAndMonth(userId, year, month);
    }

    @Override
    public Map<Category, BigDecimal> sumGroupByCategoryAndMonth(UUID userId, int year, int month) {
        log.debug("Summing transactions by category for userId={}, year={}, month={}", userId, year, month);
        List<Object[]> results = jpaRepository.sumGroupByCategoryAndMonth(userId, year, month);
        Map<Category, BigDecimal> map = new HashMap<>();
        for (Object[] row : results) {
            map.put((Category) row[0], (BigDecimal) row[1]);
        }
        return map;
    }

    @Override
    public Map<Integer, BigDecimal> sumByDayAndMonth(UUID userId, int year, int month) {
        log.debug("Summing transactions by day for userId={}, year={}, month={}", userId, year, month);
        List<Object[]> results = jpaRepository.sumByDayAndMonth(userId, year, month);
        Map<Integer, BigDecimal> map = new HashMap<>();
        for (Object[] row : results) {
            map.put((Integer) row[0], (BigDecimal) row[1]);
        }
        return map;
    }

    @Override
    public List<Transaction> findTopByUserIdAndMonth(UUID userId, int year, int month, int limit) {
        log.debug("Finding top {} transactions for userId={}, year={}, month={}", limit, userId, year, month);
        return jpaRepository.findTopByUserIdAndMonth(userId, year, month, PageRequest.of(0, limit))
                .stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }

    @Override
    public long countByUserIdAndMonth(UUID userId, int year, int month) {
        log.debug("Counting transactions for userId={}, year={}, month={}", userId, year, month);
        return jpaRepository.countByUserIdAndMonth(userId, year, month);
    }

    private TransactionPage toTransactionPage(Page<TransactionEntity> entities, int page, int size) {
        List<Transaction> content = entities.stream()
                .map(TransactionEntity::toDomain)
                .toList();
        return new TransactionPage(content, entities.getTotalElements(), page, size);
    }
}
