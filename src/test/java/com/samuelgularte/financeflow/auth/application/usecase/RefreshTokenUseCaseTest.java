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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    private static final String OLD_TOKEN_VALUE = "old-refresh-token-uuid";
    private static final String USERNAME = "joao";
    private static final String NEW_JWT = "new.jwt.token";

    private final User testUser = User.create(USERNAME, "joao@email.com", "encoded");

    private RefreshTokenRequest buildRequest() {
        return new RefreshTokenRequest(OLD_TOKEN_VALUE);
    }

    private RefreshToken createValidToken() {
        return RefreshToken.create(OLD_TOKEN_VALUE, Instant.now().plus(1, ChronoUnit.DAYS), testUser.id());
    }

    private RefreshToken createExpiredToken() {
        return RefreshToken.create(OLD_TOKEN_VALUE, Instant.now().minus(1, ChronoUnit.DAYS), testUser.id());
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should return TokenResponse with new access and refresh tokens")
        void shouldReturnNewTokens() {
            RefreshToken oldToken = createValidToken();
            when(refreshTokenRepository.findByToken(TokenHasher.hash(OLD_TOKEN_VALUE))).thenReturn(Optional.of(oldToken));
            when(userRepository.findById(testUser.id())).thenReturn(Optional.of(testUser));
            when(tokenProvider.generateTokenFromUsername(USERNAME)).thenReturn(NEW_JWT);

            TokenResponse response = refreshTokenUseCase.execute(buildRequest());

            assertEquals(NEW_JWT, response.token());
            assertEquals("Bearer", response.type());
            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            assertDoesNotThrow(() -> UUID.fromString(response.refreshToken()));
            assertEquals(TokenHasher.hash(response.refreshToken()), refreshTokenCaptor.getValue().token());
        }

        @Test
        @DisplayName("should delete old token before saving new one")
        void shouldDeleteOldToken() {
            RefreshToken oldToken = createValidToken();
            when(refreshTokenRepository.findByToken(TokenHasher.hash(OLD_TOKEN_VALUE))).thenReturn(Optional.of(oldToken));
            when(userRepository.findById(testUser.id())).thenReturn(Optional.of(testUser));
            when(tokenProvider.generateTokenFromUsername(USERNAME)).thenReturn(NEW_JWT);

            refreshTokenUseCase.execute(buildRequest());

            InOrder inOrder = inOrder(refreshTokenRepository);
            inOrder.verify(refreshTokenRepository).delete(same(oldToken));
            inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("should generate UUID refresh token with 7-day expiry")
        void shouldGenerateUuidWith7DayExpiry() {
            RefreshToken oldToken = createValidToken();
            when(refreshTokenRepository.findByToken(TokenHasher.hash(OLD_TOKEN_VALUE))).thenReturn(Optional.of(oldToken));
            when(userRepository.findById(testUser.id())).thenReturn(Optional.of(testUser));
            when(tokenProvider.generateTokenFromUsername(USERNAME)).thenReturn(NEW_JWT);

            refreshTokenUseCase.execute(buildRequest());

            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            RefreshToken savedToken = refreshTokenCaptor.getValue();
            assertEquals(64, savedToken.token().length());
            assertTrue(savedToken.token().matches("[0-9a-f]{64}"));
            Instant expectedExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
            long diffSeconds = ChronoUnit.SECONDS.between(savedToken.expiryDate(), expectedExpiry);
            assertTrue(Math.abs(diffSeconds) < 5);
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        @DisplayName("should throw InvalidRefreshTokenException when token is not found")
        void shouldThrowWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken(TokenHasher.hash(OLD_TOKEN_VALUE))).thenReturn(Optional.empty());

            InvalidRefreshTokenException ex = assertThrows(InvalidRefreshTokenException.class,
                    () -> refreshTokenUseCase.execute(buildRequest()));

            assertEquals("Invalid refresh token", ex.getMessage());
            verify(refreshTokenRepository, never()).delete(any());
            verifyNoInteractions(tokenProvider);
        }

        @Test
        @DisplayName("should throw InvalidRefreshTokenException when token is expired")
        void shouldThrowWhenTokenExpired() {
            RefreshToken expiredToken = createExpiredToken();
            when(refreshTokenRepository.findByToken(TokenHasher.hash(OLD_TOKEN_VALUE))).thenReturn(Optional.of(expiredToken));

            InvalidRefreshTokenException ex = assertThrows(InvalidRefreshTokenException.class,
                    () -> refreshTokenUseCase.execute(buildRequest()));

            assertEquals("Invalid refresh token", ex.getMessage());
            verify(refreshTokenRepository).delete(same(expiredToken));
            verify(tokenProvider, never()).generateTokenFromUsername(any());
            verify(refreshTokenRepository, never()).save(any());
        }
    }
}
