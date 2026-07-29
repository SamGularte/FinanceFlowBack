package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.output.TokenResponse;
import com.samuelgularte.financeflow.auth.application.port.AuthenticationPort;
import com.samuelgularte.financeflow.auth.application.port.TokenProvider;
import com.samuelgularte.financeflow.auth.application.usecase.request.LoginRequest;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidCredentialsException;
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
public class LoginUseCase {

    private final AuthenticationPort authenticationPort;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public LoginUseCase(AuthenticationPort authenticationPort,
                        RefreshTokenRepository refreshTokenRepository,
                        UserRepository userRepository,
                        TokenProvider tokenProvider) {
        this.authenticationPort = authenticationPort;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public TokenResponse execute(LoginRequest loginRequest) {
        String username = authenticationPort.authenticate(
                loginRequest.login(), loginRequest.password());
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        refreshTokenRepository.deleteByUserId(user.id());
        String rawToken = UUID.randomUUID().toString();
        String hashedToken = TokenHasher.hash(rawToken);
        Instant expiryDate = Instant.now().plus(7, ChronoUnit.DAYS);
        RefreshToken refreshToken = RefreshToken.create(hashedToken, expiryDate, user.id());
        refreshTokenRepository.save(refreshToken);
        String token = tokenProvider.generateTokenFromUsername(username);
        return TokenResponse.of(token, rawToken);
    }
}
