package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.port.EmailSender;
import com.samuelgularte.financeflow.auth.domain.exception.EmailSendException;
import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.PasswordResetTokenRepository;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
public class ForgotPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailSender emailSender;

    private final SecureRandom secureRandom = new SecureRandom();

    public ForgotPasswordUseCase(UserRepository userRepository,
                                 PasswordResetTokenRepository passwordResetTokenRepository,
                                 EmailSender emailSender) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailSender = emailSender;
    }

    public String execute (String email) {
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            passwordResetTokenRepository.deleteByUser(user);
            String token = generateToken();
            Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
            PasswordResetToken resetToken = new PasswordResetToken(token, expiryDate, user);
            passwordResetTokenRepository.save(resetToken);
            try {
                emailSender.sendPasswordResetEmail(email, resetToken.getToken());
            } catch (Exception e) {
                throw new EmailSendException(email);
            }
        }
        return "Password reset token sent";
    }

    private String generateToken() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
