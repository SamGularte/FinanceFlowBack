package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.port.TokenProvider;
import com.samuelgularte.financeflow.auth.application.usecase.request.RefreshTokenRequest;
import com.samuelgularte.financeflow.auth.application.usecase.response.RefreshTokenResponse;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidRefreshTokenException;
import com.samuelgularte.financeflow.auth.domain.model.RefreshToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.domain.service.TokenHasher;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                                TokenProvider tokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
    }

    public RefreshTokenResponse execute(RefreshTokenRequest request) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(TokenHasher.hash(request.getToken()))
                .orElseThrow(InvalidRefreshTokenException::new);

        if(oldToken.getExpiryDate().isBefore(Instant.now())){
            refreshTokenRepository.delete(oldToken);
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.delete(oldToken);

        User user = oldToken.getUser();
        String newAccessToken = tokenProvider.generateTokenFromUsername(user.getUserName());

        String newRawToken = UUID.randomUUID().toString();
        String newHashedToken = TokenHasher.hash(newRawToken);
        Instant newExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
        RefreshToken newRefreshToken = new RefreshToken(newHashedToken, newExpiry, user);
        refreshTokenRepository.save(newRefreshToken);

        return new RefreshTokenResponse(newAccessToken, newRawToken);
    }
}
