package com.samuelgularte.financeflow.auth.infrastructure.persistence.entity;

import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "password_reset_tokens", uniqueConstraints = {
        @UniqueConstraint(columnNames = "token"),
})
public class PasswordResetTokenEntity {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false, name = "token")
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    public static PasswordResetTokenEntity from(PasswordResetToken token, UserEntity user) {
        return new PasswordResetTokenEntity(
                token.id(),
                token.token(),
                token.expiryDate(),
                user
        );
    }

    public PasswordResetToken toDomain() {
        return new PasswordResetToken(this.id, this.token, this.user.getId(), this.expiryDate);
    }
}
