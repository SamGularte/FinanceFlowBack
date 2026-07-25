package com.samuelgularte.financeflow.auth.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.*;
import com.samuelgularte.financeflow.auth.application.usecase.request.*;
import com.samuelgularte.financeflow.auth.application.usecase.response.LoginResponse;
import com.samuelgularte.financeflow.auth.application.usecase.response.RefreshTokenResponse;
import com.samuelgularte.financeflow.auth.domain.exception.*;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetailsService;
import com.samuelgularte.financeflow.auth.infrastructure.security.JwtUtils;
import com.samuelgularte.financeflow.auth.infrastructure.security.RateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.TestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SignUpUseCase signUpUseCase;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @MockitoBean
    private ResetPasswordUseCase resetPasswordUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        rateLimitFilter.reset();
    }

    static class TestConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
    }

    @Nested
    @DisplayName("POST /auth/public/signup")
    class SignUp {

        @Test
        @DisplayName("should return 201 when data is valid")
        void shouldReturn201() throws Exception {
            when(signUpUseCase.execute(any(SignUpRequest.class))).thenReturn("User created");

            mockMvc.perform(post("/auth/public/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userName\":\"joao\",\"email\":\"joao@email.com\",\"password\":\"pass123\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("User created"));
        }

        @Test
        @DisplayName("should return 409 when username already exists")
        void shouldReturn409WhenUsernameExists() throws Exception {
            when(signUpUseCase.execute(any(SignUpRequest.class)))
                    .thenThrow(new UsernameAlreadyExistsException("joao"));

            mockMvc.perform(post("/auth/public/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userName\":\"joao\",\"email\":\"joao@email.com\",\"password\":\"pass123\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Username 'joao' already taken"));
        }

        @Test
        @DisplayName("should return 409 when email already exists")
        void shouldReturn409WhenEmailExists() throws Exception {
            when(signUpUseCase.execute(any(SignUpRequest.class)))
                    .thenThrow(new EmailAlreadyRegisteredException("joao@email.com"));

            mockMvc.perform(post("/auth/public/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userName\":\"joao\",\"email\":\"joao@email.com\",\"password\":\"pass123\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Email: joao@email.com already registered"));
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void shouldReturn400WhenInvalid() throws Exception {
            mockMvc.perform(post("/auth/public/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userName\":\"\",\"email\":\"invalid\",\"password\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /auth/public/signin")
    class SignIn {

        @Test
        @DisplayName("should return 200 with tokens when credentials are valid")
        void shouldReturn200() throws Exception {
            when(loginUseCase.execute(any(LoginRequest.class)))
                    .thenReturn(new LoginResponse("jwt-token", "refresh-token-uuid"));

            mockMvc.perform(post("/auth/public/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"login\":\"joao\",\"password\":\"pass123\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"))
                    .andExpect(jsonPath("$.type").value("Bearer"));
        }

        @Test
        @DisplayName("should return 401 when credentials are invalid")
        void shouldReturn401() throws Exception {
            when(loginUseCase.execute(any(LoginRequest.class)))
                    .thenThrow(new InvalidCredentialsException());

            mockMvc.perform(post("/auth/public/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"login\":\"joao\",\"password\":\"wrong\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid Credentials"));
        }
    }

    @Nested
    @DisplayName("POST /auth/public/forgot-password")
    class ForgotPassword {

        @Test
        @DisplayName("should return 202 when email exists or not")
        void shouldReturn202() throws Exception {
            when(forgotPasswordUseCase.execute(any(String.class))).thenReturn("Password reset token sent");

            mockMvc.perform(post("/auth/public/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"joao@email.com\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value("Password reset token sent"));
        }

        @Test
        @DisplayName("should return 500 when email sending fails")
        void shouldReturn500WhenEmailFails() throws Exception {
            when(forgotPasswordUseCase.execute(any(String.class)))
                    .thenThrow(new EmailSendException("joao@email.com"));

            mockMvc.perform(post("/auth/public/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"joao@email.com\"}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("Failed to send email to joao@email.com"));
        }
    }

    @Nested
    @DisplayName("POST /auth/public/reset-password")
    class ResetPassword {

        @Test
        @DisplayName("should return 202 when token is valid")
        void shouldReturn202() throws Exception {
            when(resetPasswordUseCase.execute(any(String.class), any(String.class)))
                    .thenReturn("Password updated");

            mockMvc.perform(post("/auth/public/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"valid-token\",\"newPassword\":\"newPass123\"}"))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.message").value("Password updated"));
        }

        @Test
        @DisplayName("should return 400 when token is invalid or expired")
        void shouldReturn400WhenInvalidToken() throws Exception {
            when(resetPasswordUseCase.execute(any(String.class), any(String.class)))
                    .thenThrow(new InvalidResetTokenException());

            mockMvc.perform(post("/auth/public/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"invalid-token\",\"newPassword\":\"newPass123\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
        }
    }

    @Nested
    @DisplayName("POST /auth/public/refresh-token")
    class RefreshToken {

        @Test
        @DisplayName("should return 200 when token is valid")
        void shouldReturn200() throws Exception {
            when(refreshTokenUseCase.execute(any(RefreshTokenRequest.class)))
                    .thenReturn(new RefreshTokenResponse("new-jwt", "new-refresh-uuid"));

            mockMvc.perform(post("/auth/public/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"valid-refresh-token\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("new-jwt"))
                    .andExpect(jsonPath("$.refreshToken").value("new-refresh-uuid"))
                    .andExpect(jsonPath("$.type").value("Bearer"));
        }

        @Test
        @DisplayName("should return 401 when token is invalid or expired")
        void shouldReturn401WhenInvalidToken() throws Exception {
            when(refreshTokenUseCase.execute(any(RefreshTokenRequest.class)))
                    .thenThrow(new InvalidRefreshTokenException());

            mockMvc.perform(post("/auth/public/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"invalid-refresh-token\"}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid refresh token"));
        }
    }

    @Nested
    @DisplayName("POST /auth/public/logout")
    class Logout {

        @Test
        @DisplayName("should return 200 when token exists or not")
        void shouldReturn200() throws Exception {
            when(logoutUseCase.execute(any(LogoutRequest.class))).thenReturn("Logged out");

            mockMvc.perform(post("/auth/public/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"some-token\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out"));
        }
    }
}
