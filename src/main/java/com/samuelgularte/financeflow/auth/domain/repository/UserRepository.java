package com.samuelgularte.financeflow.auth.domain.repository;

import com.samuelgularte.financeflow.auth.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUserName(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUserName(String username);

    User save(User user);
}
