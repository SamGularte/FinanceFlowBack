package com.samuelgularte.financeflow.auth.infrastructure.persistence.entity;

import com.samuelgularte.financeflow.auth.domain.model.RefreshToken;
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
@Table(name = "refresh_tokens", uniqueConstraints = {
        @UniqueConstraint(columnNames = "token"),
})
public class RefreshTokenEntity {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false, name = "token")
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(updatable = false)
    private Instant expiryDate;

    public static RefreshTokenEntity from(RefreshToken refreshToken, UserEntity user) {
        return new RefreshTokenEntity(
                refreshToken.id(),
                refreshToken.token(),
                user,
                refreshToken.expiryDate()
        );
    }

    public RefreshToken toDomain() {
        return new RefreshToken(this.id, this.token, this.user.getId(), this.expiryDate);
    }
}
