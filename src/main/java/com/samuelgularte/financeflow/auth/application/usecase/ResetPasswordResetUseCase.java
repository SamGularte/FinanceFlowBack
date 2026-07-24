package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.PasswordResetToken;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.User;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.repository.PasswordResetTokenRepository;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@Transactional
public class ResetPasswordResetUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordResetUseCase(UserRepository userRepository,
                                     PasswordResetTokenRepository passwordResetTokenRepository,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String execute (String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password reset token"));

        if(resetToken.isUsed()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token has already been used");
        }

        if(resetToken.getExpiryDate().isBefore(Instant.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return "Password updated";
    }
}
