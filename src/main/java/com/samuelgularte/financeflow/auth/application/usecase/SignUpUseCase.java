package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.domain.exception.EmailAlreadyRegisteredException;
import com.samuelgularte.financeflow.auth.domain.exception.UsernameAlreadyExistsException;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.entity.User;
import com.samuelgularte.financeflow.auth.application.usecase.request.SignUpRequest;
import com.samuelgularte.financeflow.auth.infrastructure.persistence.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SignUpUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignUpUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String execute(SignUpRequest request) {
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new UsernameAlreadyExistsException(request.getUserName());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyRegisteredException(request.getEmail());
        }

        User user = new User(
                request.getUserName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);
        return "User created";
    }
}
