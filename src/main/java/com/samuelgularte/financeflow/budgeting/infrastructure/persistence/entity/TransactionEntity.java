package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity;

import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private long amount;

    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static TransactionEntity from(Transaction transaction, User user) {
        return new TransactionEntity(
                transaction.id(),
                transaction.description(),
                transaction.amount(),
                transaction.category(),
                user
        );
    }

    public Transaction toDomain() {
        return new Transaction(
                this.id,
                this.description,
                this.amount,
                this.category,
                this.user.getId()
        );
    }
}
