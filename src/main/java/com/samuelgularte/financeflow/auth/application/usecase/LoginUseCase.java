package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.usecase.request.LoginRequest;
import com.samuelgularte.financeflow.auth.application.usecase.response.LoginResponse;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidCredentialsException;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.RefreshToken;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.User;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.repository.UserRepository;
import com.samuelgularte.financeflow.auth.infrastructure.security.JwtUtils;
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
    private final JwtUtils jwtUtils;

    public LoginUseCase(AuthenticationManager authenticationManager,
                        RefreshTokenRepository refreshTokenRepository,
                        UserRepository userRepository,
                        JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse execute(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            User user = userRepository.findByUserName(authentication.getName()).orElseThrow();
            refreshTokenRepository.deleteByUser(user);
            String refreshTokenValue = UUID.randomUUID().toString();
            Instant expiryDate = Instant.now().plus(7, ChronoUnit.DAYS);
            RefreshToken refreshToken = new RefreshToken(refreshTokenValue, expiryDate, user);
            refreshTokenRepository.save(refreshToken);
            String token = jwtUtils.generateTokenFromUsername(authentication.getName());
            return new LoginResponse(token, refreshTokenValue);
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
    }
}
