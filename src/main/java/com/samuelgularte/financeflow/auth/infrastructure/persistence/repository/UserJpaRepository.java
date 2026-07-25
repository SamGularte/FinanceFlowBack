package com.samuelgularte.financeflow.auth.infrastructure.persistence.repository;

import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<User, UUID>, UserRepository {

    Optional<User> findByUserName(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUserName(String username);
}
