package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.port.PasswordEncoderPort;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidResetTokenException;
import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.PasswordResetTokenRepository;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
import com.samuelgularte.financeflow.auth.domain.service.TokenHasher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private ResetPasswordUseCase resetPasswordUseCase;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private static final String TOKEN = "reset-token-value";
    private static final String HASHED_TOKEN = TokenHasher.hash(TOKEN);
    private static final String NEW_PASSWORD = "NovaSenha123!";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";
    private static final String USERNAME = "joao";

    private final User testUser = User.create(USERNAME, "joao@email.com", "old-encoded");

    private PasswordResetToken createValidToken() {
        return PasswordResetToken.create(TOKEN, Instant.now().plus(1, ChronoUnit.DAYS), testUser.id());
    }

    private PasswordResetToken createExpiredToken() {
        return PasswordResetToken.create(TOKEN, Instant.now().minus(1, ChronoUnit.DAYS), testUser.id());
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should encode password, save user, delete token and return message")
        void shouldUpdatePasswordAndDeleteToken() {
            PasswordResetToken resetToken = createValidToken();
            when(passwordResetTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(resetToken));
            when(userRepository.findById(testUser.id())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            String result = resetPasswordUseCase.execute(TOKEN, NEW_PASSWORD);

            assertEquals("Password updated", result);
            verify(passwordEncoder).encode(NEW_PASSWORD);
            verify(userRepository).save(userCaptor.capture());
            assertEquals(ENCODED_PASSWORD, userCaptor.getValue().password());
            verify(passwordResetTokenRepository).delete(same(resetToken));
        }

        @Test
        @DisplayName("should delete the reset token after updating the password")
        void shouldDeleteTokenAfterUpdate() {
            PasswordResetToken resetToken = createValidToken();
            when(passwordResetTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(resetToken));
            when(userRepository.findById(testUser.id())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            resetPasswordUseCase.execute(TOKEN, NEW_PASSWORD);

            verify(userRepository).save(any(User.class));
            verify(passwordResetTokenRepository).delete(same(resetToken));
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        @DisplayName("should throw InvalidResetTokenException when token is not found")
        void shouldThrowWhenTokenNotFound() {
            when(passwordResetTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.empty());

            InvalidResetTokenException ex = assertThrows(InvalidResetTokenException.class,
                    () -> resetPasswordUseCase.execute(TOKEN, NEW_PASSWORD));

            assertEquals("Invalid or expired password reset token", ex.getMessage());
            verifyNoInteractions(passwordEncoder, userRepository);
            verify(passwordResetTokenRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should throw InvalidResetTokenException when token is expired and delete it")
        void shouldThrowWhenTokenExpired() {
            PasswordResetToken expiredToken = createExpiredToken();
            when(passwordResetTokenRepository.findByToken(HASHED_TOKEN)).thenReturn(Optional.of(expiredToken));

            InvalidResetTokenException ex = assertThrows(InvalidResetTokenException.class,
                    () -> resetPasswordUseCase.execute(TOKEN, NEW_PASSWORD));

            assertEquals("Invalid or expired password reset token", ex.getMessage());
            verify(passwordResetTokenRepository).delete(same(expiredToken));
            verifyNoInteractions(passwordEncoder, userRepository);
            verify(userRepository, never()).save(any());
        }
    }
}
