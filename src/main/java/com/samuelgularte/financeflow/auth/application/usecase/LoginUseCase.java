package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.port.TokenProvider;
import com.samuelgularte.financeflow.auth.application.usecase.request.LoginRequest;
import com.samuelgularte.financeflow.auth.application.usecase.response.TokenResponse;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidCredentialsException;
import com.samuelgularte.financeflow.auth.domain.model.RefreshToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
import com.samuelgularte.financeflow.auth.domain.service.TokenHasher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public LoginUseCase(AuthenticationManager authenticationManager,
                        RefreshTokenRepository refreshTokenRepository,
                        UserRepository userRepository,
                        TokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public TokenResponse execute(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword())
            );
            User user = userRepository.findByUserName(authentication.getName())
                    .orElseThrow(InvalidCredentialsException::new);
            refreshTokenRepository.deleteByUser(user);
            String rawToken = UUID.randomUUID().toString();
            String hashedToken = TokenHasher.hash(rawToken);
            Instant expiryDate = Instant.now().plus(7, ChronoUnit.DAYS);
            RefreshToken refreshToken = new RefreshToken(hashedToken, expiryDate, user);
            refreshTokenRepository.save(refreshToken);
            String token = tokenProvider.generateTokenFromUsername(authentication.getName());
            return new TokenResponse(token, rawToken);
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
    }
}
