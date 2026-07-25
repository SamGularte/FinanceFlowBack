package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.usecase.request.SignUpRequest;
import com.samuelgularte.financeflow.auth.domain.exception.EmailAlreadyRegisteredException;
import com.samuelgularte.financeflow.auth.domain.exception.UsernameAlreadyExistsException;
import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUpUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignUpUseCase signUpUseCase;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private static final String USERNAME = "joao";
    private static final String EMAIL = "joao@email.com";
    private static final String RAW_PASSWORD = "senha123";
    private static final String ENCODED_PASSWORD = "$2a$10$encoded";

    private SignUpRequest buildValidRequest() {
        SignUpRequest request = new SignUpRequest();
        request.setUserName(USERNAME);
        request.setEmail(EMAIL);
        request.setPassword(RAW_PASSWORD);
        return request;
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should save user and return message when username and email are unique")
        void shouldSaveUserAndReturnMessage() {
            SignUpRequest request = buildValidRequest();
            when(userRepository.existsByUserName(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            String result = signUpUseCase.execute(request);

            assertEquals("User created", result);
            verify(passwordEncoder).encode(RAW_PASSWORD);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertEquals(USERNAME, savedUser.getUserName());
            assertEquals(EMAIL, savedUser.getEmail());
            assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        @DisplayName("should throw UsernameAlreadyExistsException when username is taken")
        void shouldThrowWhenUsernameExists() {
            SignUpRequest request = buildValidRequest();
            when(userRepository.existsByUserName(USERNAME)).thenReturn(true);

            UsernameAlreadyExistsException ex = assertThrows(UsernameAlreadyExistsException.class, () -> signUpUseCase.execute(request));

            assertTrue(ex.getMessage().contains(USERNAME));
            verify(userRepository, never()).existsByEmail(any());
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw EmailAlreadyRegisteredException when email is already registered")
        void shouldThrowWhenEmailExists() {
            SignUpRequest request = buildValidRequest();
            when(userRepository.existsByUserName(USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            EmailAlreadyRegisteredException ex = assertThrows(EmailAlreadyRegisteredException.class, () -> signUpUseCase.execute(request));

            assertTrue(ex.getMessage().contains(EMAIL));
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }
    }
}
