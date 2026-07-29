package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    private UUID id;
    private String description;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static TransactionEntity from(Transaction transaction, UserEntity user) {
        return new TransactionEntity(
                transaction.id(),
                transaction.description(),
                transaction.amount(),
                transaction.category(),
                user,
                transaction.createdAt()
        );
    }

    public Transaction toDomain() {
        return new Transaction(
                this.id,
                this.description,
                this.amount,
                this.category,
                this.user.getId(),
                this.createdAt
        );
    }
}
