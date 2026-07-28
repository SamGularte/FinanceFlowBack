package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.port.PasswordEncoderPort;
import com.samuelgularte.financeflow.auth.domain.exception.EmailAlreadyRegisteredException;
import com.samuelgularte.financeflow.auth.domain.exception.UsernameAlreadyExistsException;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
import com.samuelgularte.financeflow.auth.application.usecase.request.SignUpRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SignUpUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public SignUpUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String execute(SignUpRequest request) {
        if (userRepository.existsByUserName(request.userName())) {
            throw new UsernameAlreadyExistsException(request.userName());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }

        User user = User.create(
                request.userName(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        userRepository.save(user);
        return "User created";
    }
}
