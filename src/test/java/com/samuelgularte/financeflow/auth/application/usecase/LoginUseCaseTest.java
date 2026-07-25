package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.port.TokenProvider;
import com.samuelgularte.financeflow.auth.application.usecase.request.LoginRequest;
import com.samuelgularte.financeflow.auth.application.usecase.response.LoginResponse;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidCredentialsException;
import com.samuelgularte.financeflow.auth.domain.model.RefreshToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

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
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private LoginUseCase loginUseCase;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    private static final String USERNAME = "joao";
    private static final String EMAIL = "joao@email.com";
    private static final String PASSWORD = "senha123";
    private static final String JWT_TOKEN = "jwt.token.here";

    private LoginRequest buildValidRequest() {
        LoginRequest request = new LoginRequest();
        request.setLogin(USERNAME);
        request.setPassword(PASSWORD);
        return request;
    }

    private LoginRequest buildEmailRequest() {
        LoginRequest request = new LoginRequest();
        request.setLogin(EMAIL);
        request.setPassword(PASSWORD);
        return request;
    }

    private User createUser() {
        return new User(USERNAME, EMAIL, "encoded");
    }

    private User mockSuccess() {
        User user = createUser();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn(USERNAME);
        when(userRepository.findByUserName(USERNAME)).thenReturn(Optional.of(user));
        when(tokenProvider.generateTokenFromUsername(USERNAME)).thenReturn(JWT_TOKEN);
        return user;
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should return LoginResponse with JWT and refresh token")
        void shouldReturnLoginResponseWithTokens() {
            mockSuccess();

            LoginResponse response = loginUseCase.execute(buildValidRequest());

            assertEquals(JWT_TOKEN, response.getToken());
            assertEquals("Bearer", response.getType());
            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            assertEquals(refreshTokenCaptor.getValue().getToken(), response.getRefreshToken());
        }

        @Test
        @DisplayName("should delete old refresh tokens before creating a new one")
        void shouldDeleteOldTokensBeforeCreatingNew() {
            User user = mockSuccess();

            loginUseCase.execute(buildValidRequest());

            InOrder inOrder = inOrder(refreshTokenRepository);
            inOrder.verify(refreshTokenRepository).deleteByUser(same(user));
            inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("should generate UUID refresh token with 7-day expiry")
        void shouldGenerateUuidTokenWith7DayExpiry() {
            mockSuccess();

            loginUseCase.execute(buildValidRequest());

            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            RefreshToken savedToken = refreshTokenCaptor.getValue();
            assertDoesNotThrow(() -> UUID.fromString(savedToken.getToken()));
            Instant expectedExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
            long diffSeconds = ChronoUnit.SECONDS.between(savedToken.getExpiryDate(), expectedExpiry);
            assertTrue(Math.abs(diffSeconds) < 5);
        }

        @Test
        @DisplayName("should login successfully when using email instead of username")
        void shouldLoginWithEmail() {
            mockSuccess();

            LoginResponse response = loginUseCase.execute(buildEmailRequest());

            assertEquals(JWT_TOKEN, response.getToken());
            verify(authenticationManager).authenticate(
                    new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
            verify(userRepository).findByUserName(USERNAME);
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        @DisplayName("should throw InvalidCredentialsException when authentication fails")
        void shouldThrowWhenInvalidCredentials() {
            LoginRequest request = buildValidRequest();
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(mock(AuthenticationException.class));

            InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(request));

            assertEquals("Invalid Credentials", ex.getMessage());
            verifyNoInteractions(userRepository, refreshTokenRepository, tokenProvider);
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when authenticated user is not found in database")
        void shouldThrowWhenUserNotFoundAfterAuthentication() {
            LoginRequest request = buildValidRequest();
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getName()).thenReturn(USERNAME);
            when(userRepository.findByUserName(USERNAME)).thenReturn(Optional.empty());

            InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> loginUseCase.execute(request));

            assertEquals("Invalid Credentials", ex.getMessage());
            verifyNoInteractions(refreshTokenRepository, tokenProvider);
        }
    }
}
