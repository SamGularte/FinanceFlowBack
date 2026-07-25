package com.samuelgularte.financeflow.auth.domain.repository;

import com.samuelgularte.financeflow.auth.domain.model.RefreshToken;
import com.samuelgularte.financeflow.auth.domain.model.User;

import java.util.Optional;

public interface RefreshTokenRepository {

    Optional<RefreshToken> findByToken(String token);

    RefreshToken save(RefreshToken refreshToken);

    void delete(RefreshToken refreshToken);

    void deleteByUser(User user);
}
