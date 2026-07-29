package com.samuelgularte.financeflow.auth.infrastructure.persistence.entity;

import com.samuelgularte.financeflow.auth.domain.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class UserEntity {

    @Id
    private UUID id;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 120)
    private String password;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime updatedDate;

    public static UserEntity from(User user) {
        return new UserEntity(
                user.id(),
                user.username(),
                user.email(),
                user.password(),
                user.createdDate(),
                user.updatedDate()
        );
    }

    public User toDomain() {
        return new User(this.id, this.username, this.email, this.password, this.createdDate, this.updatedDate);
    }
}
