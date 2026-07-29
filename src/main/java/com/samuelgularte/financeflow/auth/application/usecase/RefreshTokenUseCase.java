package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.output.TokenResponse;
import com.samuelgularte.financeflow.auth.application.port.TokenProvider;
import com.samuelgularte.financeflow.auth.application.usecase.request.RefreshTokenRequest;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidRefreshTokenException;
import com.samuelgularte.financeflow.auth.domain.model.RefreshToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
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
    private final UserRepository userRepository;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                                TokenProvider tokenProvider,
                                UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public TokenResponse execute(RefreshTokenRequest request) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(TokenHasher.hash(request.token()))
                .orElseThrow(InvalidRefreshTokenException::new);

        if (oldToken.expiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(oldToken);
            throw new InvalidRefreshTokenException();
        }

        refreshTokenRepository.delete(oldToken);

        User user = userRepository.findById(oldToken.userId())
                .orElseThrow(InvalidRefreshTokenException::new);
        String newAccessToken = tokenProvider.generateTokenFromUsername(user.username());

        String newRawToken = UUID.randomUUID().toString();
        String newHashedToken = TokenHasher.hash(newRawToken);
        Instant newExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
        RefreshToken newRefreshToken = RefreshToken.create(newHashedToken, newExpiry, user.id());
        refreshTokenRepository.save(newRefreshToken);

        return TokenResponse.of(newAccessToken, newRawToken);
    }
}
