package com.samuelgularte.financeflow.auth.infrastructure.security;

import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private static final String USERNAME = "joao";
    private static final String EMAIL = "joao@email.com";

    private User createUser() {
        return new User(USERNAME, EMAIL, "encoded");
    }

    @Nested
    @DisplayName("Success")
    class Success {

        @Test
        @DisplayName("should load user by username")
        void shouldLoadByUsername() {
            User user = createUser();
            when(userRepository.findByUserName(USERNAME)).thenReturn(Optional.of(user));

            UserDetails result = customUserDetailsService.loadUserByUsername(USERNAME);

            assertEquals(USERNAME, result.getUsername());
            verify(userRepository).findByUserName(USERNAME);
            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("should load user by email when username is not found")
        void shouldLoadByEmail() {
            User user = createUser();
            when(userRepository.findByUserName(EMAIL)).thenReturn(Optional.empty());
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            UserDetails result = customUserDetailsService.loadUserByUsername(EMAIL);

            assertEquals(USERNAME, result.getUsername());
            verify(userRepository).findByUserName(EMAIL);
            verify(userRepository).findByEmail(EMAIL);
        }
    }

    @Nested
    @DisplayName("Exceptions")
    class Exceptions {

        @Test
        @DisplayName("should throw UsernameNotFoundException when neither username nor email exists")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByUserName("unknown")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () ->
                    customUserDetailsService.loadUserByUsername("unknown"));
        }
    }
}