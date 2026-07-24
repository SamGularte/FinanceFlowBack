package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.usecase.request.RefreshTokenRequest;
import com.samuelgularte.financeflow.auth.application.usecase.response.RefreshTokenResponse;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidRefreshTokenException;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.RefreshToken;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.User;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.infrastructure.security.JwtUtils;
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
    private JwtUtils jwtUtils;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    private static final String OLD_TOKEN_VALUE = "old-refresh-token-uuid";
    private static final String USERNAME = "joao";
    private static final String NEW_JWT = "new.jwt.token";

    private RefreshTokenRequest buildRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setToken(OLD_TOKEN_VALUE);
        return request;
    }

    private User createUser() {
        return new User(USERNAME, "joao@email.com", "encoded");
    }

    private RefreshToken createValidToken() {
        return new RefreshToken(OLD_TOKEN_VALUE, Instant.now().plus(1, ChronoUnit.DAYS), createUser());
    }

    private RefreshToken createExpiredToken() {
        return new RefreshToken(OLD_TOKEN_VALUE, Instant.now().minus(1, ChronoUnit.DAYS), createUser());
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should return RefreshTokenResponse with new access and refresh tokens")
        void shouldReturnNewTokens() {
            RefreshToken oldToken = createValidToken();
            when(refreshTokenRepository.findByToken(OLD_TOKEN_VALUE)).thenReturn(Optional.of(oldToken));
            when(jwtUtils.generateTokenFromUsername(USERNAME)).thenReturn(NEW_JWT);

            RefreshTokenResponse response = refreshTokenUseCase.execute(buildRequest());

            assertEquals(NEW_JWT, response.getToken());
            assertEquals("Bearer", response.getType());
            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            assertEquals(refreshTokenCaptor.getValue().getToken(), response.getRefreshToken());
        }

        @Test
        @DisplayName("should delete old token before saving new one")
        void shouldDeleteOldToken() {
            RefreshToken oldToken = createValidToken();
            when(refreshTokenRepository.findByToken(OLD_TOKEN_VALUE)).thenReturn(Optional.of(oldToken));
            when(jwtUtils.generateTokenFromUsername(USERNAME)).thenReturn(NEW_JWT);

            refreshTokenUseCase.execute(buildRequest());

            InOrder inOrder = inOrder(refreshTokenRepository);
            inOrder.verify(refreshTokenRepository).delete(same(oldToken));
            inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("should generate UUID refresh token with 7-day expiry")
        void shouldGenerateUuidWith7DayExpiry() {
            RefreshToken oldToken = createValidToken();
            when(refreshTokenRepository.findByToken(OLD_TOKEN_VALUE)).thenReturn(Optional.of(oldToken));
            when(jwtUtils.generateTokenFromUsername(USERNAME)).thenReturn(NEW_JWT);

            refreshTokenUseCase.execute(buildRequest());

            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            RefreshToken savedToken = refreshTokenCaptor.getValue();
            assertDoesNotThrow(() -> UUID.fromString(savedToken.getToken()));
            Instant expectedExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
            long diffSeconds = ChronoUnit.SECONDS.between(savedToken.getExpiryDate(), expectedExpiry);
            assertTrue(Math.abs(diffSeconds) < 5);
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        @DisplayName("should throw InvalidRefreshTokenException when token is not found")
        void shouldThrowWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken(OLD_TOKEN_VALUE)).thenReturn(Optional.empty());

            InvalidRefreshTokenException ex = assertThrows(InvalidRefreshTokenException.class,
                    () -> refreshTokenUseCase.execute(buildRequest()));

            assertEquals("Invalid refresh token", ex.getMessage());
            verify(refreshTokenRepository, never()).delete(any());
            verifyNoInteractions(jwtUtils);
        }

        @Test
        @DisplayName("should throw InvalidRefreshTokenException when token is expired")
        void shouldThrowWhenTokenExpired() {
            RefreshToken expiredToken = createExpiredToken();
            when(refreshTokenRepository.findByToken(OLD_TOKEN_VALUE)).thenReturn(Optional.of(expiredToken));

            InvalidRefreshTokenException ex = assertThrows(InvalidRefreshTokenException.class,
                    () -> refreshTokenUseCase.execute(buildRequest()));

            assertEquals("Invalid refresh token", ex.getMessage());
            verify(refreshTokenRepository).delete(same(expiredToken));
            verify(jwtUtils, never()).generateTokenFromUsername(any());
            verify(refreshTokenRepository, never()).save(any());
        }
    }
}
