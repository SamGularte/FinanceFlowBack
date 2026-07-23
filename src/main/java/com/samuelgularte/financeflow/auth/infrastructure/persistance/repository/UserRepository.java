package com.samuelgularte.financeflow.auth.infrastructure.persistance.repository;

import com.samuelgularte.financeflow.auth.infrastructure.persistance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserName(String username);

    Boolean existsByEmail(String email);

    Boolean existsByUserName(String username);
}
