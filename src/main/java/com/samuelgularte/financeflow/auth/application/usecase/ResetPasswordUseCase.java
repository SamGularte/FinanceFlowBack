package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.domain.exception.InvalidResetTokenException;
import com.samuelgularte.financeflow.auth.domain.model.PasswordResetToken;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.PasswordResetTokenRepository;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Transactional
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordUseCase(UserRepository userRepository,
                                PasswordResetTokenRepository passwordResetTokenRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String execute (String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(InvalidResetTokenException::new);

        if(resetToken.getExpiryDate().isBefore(Instant.now())){
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidResetTokenException();
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        return "Password updated";
    }
}
