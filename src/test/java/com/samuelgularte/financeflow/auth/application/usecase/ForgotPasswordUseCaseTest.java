package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.port.EmailSender;
import com.samuelgularte.financeflow.auth.domain.exception.EmailSendException;
import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.PasswordResetTokenRepository;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @Captor
    private ArgumentCaptor<PasswordResetToken> resetTokenCaptor;

    private static final String EMAIL = "joao@email.com";
    private static final String USERNAME = "joao";
    private static final Pattern HEX_64 = Pattern.compile("[0-9a-f]{64}");

    private User createUser() {
        return User.create(USERNAME, EMAIL, "encoded");
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
            assertEquals(user.id(), savedToken.userId());
            verify(emailSender).sendPasswordResetEmail(eq(EMAIL), anyString());
        }

        @Test
        @DisplayName("should store 64-character hex hash with 24-hour expiry")
        void shouldGenerateHexTokenWith24HourExpiry() {
            User user = createUser();
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            forgotPasswordUseCase.execute(EMAIL);

            verify(passwordResetTokenRepository).save(resetTokenCaptor.capture());
            PasswordResetToken savedToken = resetTokenCaptor.getValue();
            assertTrue(HEX_64.matcher(savedToken.token()).matches(),
                    "Token should be 64 hex characters (SHA-256 hash)");
            Instant expectedExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
            long diffSeconds = ChronoUnit.SECONDS.between(savedToken.expiryDate(), expectedExpiry);
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
            inOrder.verify(passwordResetTokenRepository).deleteByUserId(same(user.id()));
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
            verifyNoInteractions(passwordResetTokenRepository, emailSender);
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
            doThrow(new RuntimeException("SMTP error")).when(emailSender)
                    .sendPasswordResetEmail(anyString(), anyString());

            EmailSendException ex = assertThrows(EmailSendException.class,
                    () -> forgotPasswordUseCase.execute(EMAIL));

            assertTrue(ex.getMessage().contains(EMAIL));
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        }
    }
}
