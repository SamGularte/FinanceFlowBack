package com.samuelgularte.financeflow.budgeting.infrastructure.persistence.entity;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.UserEntity;
import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monthly_insights", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "insight_year", "insight_month"})
})
public class MonthlyInsightEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "insight_year", nullable = false)
    private int year;

    @Column(name = "insight_month", nullable = false)
    private int month;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    public static MonthlyInsightEntity from(MonthlyInsight insight, UserEntity user) {
        return new MonthlyInsightEntity(
                insight.id(),
                user,
                insight.year(),
                insight.month(),
                insight.content(),
                insight.generatedAt()
        );
    }

    public MonthlyInsight toDomain() {
        return new MonthlyInsight(
                this.id,
                this.user.getId(),
                this.year,
                this.month,
                this.content,
                this.generatedAt
        );
    }
}
