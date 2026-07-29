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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private AuthenticationPort authenticationPort;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private LoginUseCase loginUseCase;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    private static final String USERNAME = "joao";
    private static final String EMAIL = "joao@email.com";
    private static final String PASSWORD = "senha123";
    private static final String JWT_TOKEN = "jwt.token.here";

    private LoginRequest buildValidRequest() {
        return new LoginRequest(USERNAME, PASSWORD);
    }

    private LoginRequest buildEmailRequest() {
        return new LoginRequest(EMAIL, PASSWORD);
    }

    private User createUser() {
        return User.create(USERNAME, EMAIL, "encoded");
    }

    private User mockSuccess() {
        User user = createUser();
        when(authenticationPort.authenticate(anyString(), anyString())).thenReturn(USERNAME);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(tokenProvider.generateTokenFromUsername(USERNAME)).thenReturn(JWT_TOKEN);
        return user;
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should return TokenResponse with JWT and refresh token")
        void shouldReturnTokenResponseWithTokens() {
            mockSuccess();

            TokenResponse response = loginUseCase.execute(buildValidRequest());

            assertEquals(JWT_TOKEN, response.token());
            assertEquals("Bearer", response.type());
            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            assertDoesNotThrow(() -> UUID.fromString(response.refreshToken()));
            assertEquals(TokenHasher.hash(response.refreshToken()), refreshTokenCaptor.getValue().token());
        }

        @Test
        @DisplayName("should delete old refresh tokens before creating a new one")
        void shouldDeleteOldTokensBeforeCreatingNew() {
            User user = mockSuccess();

            loginUseCase.execute(buildValidRequest());

            InOrder inOrder = inOrder(refreshTokenRepository);
            inOrder.verify(refreshTokenRepository).deleteByUserId(same(user.id()));
            inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("should generate UUID refresh token with 7-day expiry")
        void shouldGenerateUuidTokenWith7DayExpiry() {
            mockSuccess();

            loginUseCase.execute(buildValidRequest());

            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            RefreshToken savedToken = refreshTokenCaptor.getValue();
            assertEquals(64, savedToken.token().length());
            assertTrue(savedToken.token().matches("[0-9a-f]{64}"));
            Instant expectedExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
            long diffSeconds = ChronoUnit.SECONDS.between(savedToken.expiryDate(), expectedExpiry);
            assertTrue(Math.abs(diffSeconds) < 5);
        }

        @Test
        @DisplayName("should login successfully when using email instead of username")
        void shouldLoginWithEmail() {
            mockSuccess();

            TokenResponse response = loginUseCase.execute(buildEmailRequest());

            assertEquals(JWT_TOKEN, response.token());
            verify(authenticationPort).authenticate(EMAIL, PASSWORD);
            verify(userRepository).findByUsername(USERNAME);
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        @DisplayName("should throw InvalidCredentialsException when authentication fails")
        void shouldThrowWhenInvalidCredentials() {
            LoginRequest request = buildValidRequest();
            when(authenticationPort.authenticate(USERNAME, PASSWORD))
                    .thenThrow(new InvalidCredentialsException());

            InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(request));

            assertEquals("Invalid Credentials", ex.getMessage());
            verifyNoInteractions(userRepository, refreshTokenRepository, tokenProvider);
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when authenticated user is not found in database")
        void shouldThrowWhenUserNotFoundAfterAuthentication() {
            LoginRequest request = buildValidRequest();
            when(authenticationPort.authenticate(USERNAME, PASSWORD)).thenReturn(USERNAME);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(request));

            assertEquals("Invalid Credentials", ex.getMessage());
            verifyNoInteractions(refreshTokenRepository, tokenProvider);
        }
    }
}
