package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.usecase.request.RefreshTokenRequest;
import com.samuelgularte.financeflow.auth.application.usecase.response.RefreshTokenResponse;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.RefreshToken;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.User;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.infrastructure.security.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                               JwtUtils jwtUtils) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtils = jwtUtils;
    }

    public RefreshTokenResponse execute(RefreshTokenRequest request) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(request.getToken()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if(oldToken.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(oldToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        refreshTokenRepository.delete(oldToken);

        User user = oldToken.getUser();
        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getUserName());

        String newRefreshTokenValue = UUID.randomUUID().toString();
        Instant newExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
        RefreshToken newRefreshToken = new RefreshToken(newRefreshTokenValue, newExpiry, user);
        refreshTokenRepository.save(newRefreshToken);

        return new RefreshTokenResponse(newAccessToken, newRefreshTokenValue);
    }
}
