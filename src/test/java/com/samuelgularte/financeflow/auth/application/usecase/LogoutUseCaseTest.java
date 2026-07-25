package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.usecase.request.LogoutRequest;
import com.samuelgularte.financeflow.auth.domain.model.RefreshToken;
import com.samuelgularte.financeflow.auth.domain.repository.RefreshTokenRepository;
import com.samuelgularte.financeflow.auth.domain.service.TokenHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LogoutUseCase logoutUseCase;

    private static final String TOKEN_VALUE = "refresh-token-uuid";

    private LogoutRequest buildRequest() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken(TOKEN_VALUE);
        return request;
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should delete token and return message when token exists")
        void shouldDeleteTokenWhenExists() {
            RefreshToken foundToken = new RefreshToken(TokenHasher.hash(TOKEN_VALUE), Instant.now().plus(7, ChronoUnit.DAYS), null);
            when(refreshTokenRepository.findByToken(TokenHasher.hash(TOKEN_VALUE))).thenReturn(Optional.of(foundToken));

            String result = logoutUseCase.execute(buildRequest());

            assertEquals("Logged out", result);
            verify(refreshTokenRepository).delete(same(foundToken));
        }
    }

    @Nested
    @DisplayName("Token not found")
    class TokenNotFound {

        @Test
        @DisplayName("should return message without deleting when token does not exist")
        void shouldNotDeleteWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken(TokenHasher.hash(TOKEN_VALUE))).thenReturn(Optional.empty());

            String result = logoutUseCase.execute(buildRequest());

            assertEquals("Logged out", result);
            verify(refreshTokenRepository, never()).delete(any());
        }
    }
}
