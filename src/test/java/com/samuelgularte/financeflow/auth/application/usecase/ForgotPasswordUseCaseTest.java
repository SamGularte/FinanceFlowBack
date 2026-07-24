package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.domain.exception.EmailSendException;
import com.samuelgularte.financeflow.auth.infrastructure.email.EmailService;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.PasswordResetToken;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.User;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.repository.UserRepository;
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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @Captor
    private ArgumentCaptor<PasswordResetToken> resetTokenCaptor;

    private static final String EMAIL = "joao@email.com";
    private static final String USERNAME = "joao";
    private static final Pattern HEX_32 = Pattern.compile("[0-9a-f]{32}");

    private User createUser() {
        return new User(USERNAME, EMAIL, "encoded");
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should save token, send email and return message when email exists")
        void shouldSaveTokenAndSendEmail() {
            User user = createUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            String result = forgotPasswordUseCase.execute(EMAIL);

            assertEquals("Password reset token sent", result);
            verify(passwordResetTokenRepository).save(resetTokenCaptor.capture());
            PasswordResetToken savedToken = resetTokenCaptor.getValue();
            assertEquals(user, savedToken.getUser());
            verify(emailService).sendPasswordResetEmail(EMAIL, savedToken.getToken());
        }

        @Test
        @DisplayName("should generate 32-character hex token with 24-hour expiry")
        void shouldGenerateHexTokenWith24HourExpiry() {
            User user = createUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            forgotPasswordUseCase.execute(EMAIL);

            verify(passwordResetTokenRepository).save(resetTokenCaptor.capture());
            PasswordResetToken savedToken = resetTokenCaptor.getValue();
            assertTrue(HEX_32.matcher(savedToken.getToken()).matches(),
                    "Token should be 32 hex characters");
            Instant expectedExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
            long diffSeconds = ChronoUnit.SECONDS.between(savedToken.getExpiryDate(), expectedExpiry);
            assertTrue(Math.abs(diffSeconds) < 5,
                    "Expiry should be approximately 24 hours from now");
        }

        @Test
        @DisplayName("should delete old reset tokens before saving new one")
        void shouldDeleteOldTokensBeforeSavingNew() {
            User user = createUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            forgotPasswordUseCase.execute(EMAIL);

            InOrder inOrder = inOrder(passwordResetTokenRepository);
            inOrder.verify(passwordResetTokenRepository).deleteByUser(same(user));
            inOrder.verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }
    }

    @Nested
    @DisplayName("Email not found")
    class EmailNotFound {

        @Test
        @DisplayName("should return success message without saving token or sending email")
        void shouldReturnSuccessWhenEmailNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            String result = forgotPasswordUseCase.execute(EMAIL);

            assertEquals("Password reset token sent", result);
            verifyNoInteractions(passwordResetTokenRepository, emailService);
        }
    }

    @Nested
    @DisplayName("Email failure")
    class EmailFailure {

        @Test
        @DisplayName("should throw EmailSendException when email service fails")
        void shouldThrowWhenEmailFails() {
            User user = createUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            doThrow(new RuntimeException("SMTP error")).when(emailService)
                    .sendPasswordResetEmail(anyString(), anyString());

            EmailSendException ex = assertThrows(EmailSendException.class,
                    () -> forgotPasswordUseCase.execute(EMAIL));

            assertTrue(ex.getMessage().contains(EMAIL));
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }
    }
}
