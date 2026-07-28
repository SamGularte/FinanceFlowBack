package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.port.PasswordEncoderPort;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidResetTokenException;
import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.PasswordResetTokenRepository;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
import com.samuelgularte.financeflow.auth.domain.service.TokenHasher;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@Transactional
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoderPort passwordEncoder;

    public ResetPasswordUseCase(UserRepository userRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String execute(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(TokenHasher.hash(token))
                .orElseThrow(InvalidResetTokenException::new);

        if (resetToken.expiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidResetTokenException();
        }

        User user = userRepository.findById(resetToken.userId())
                .orElseThrow(InvalidResetTokenException::new);
        User updated = new User(user.id(), user.userName(), user.email(),
                passwordEncoder.encode(newPassword), user.createdDate(), LocalDateTime.now());
        userRepository.save(updated);

        passwordResetTokenRepository.delete(resetToken);

        return "Password updated";
    }
}
