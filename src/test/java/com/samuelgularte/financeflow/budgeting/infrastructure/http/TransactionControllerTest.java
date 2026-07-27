package com.samuelgularte.financeflow.budgeting.infrastructure.http;

import com.samuelgularte.financeflow.auth.domain.model.User;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetails;
import com.samuelgularte.financeflow.auth.infrastructure.security.CustomUserDetailsService;
import com.samuelgularte.financeflow.auth.infrastructure.security.JwtUtils;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessAudioUseCase;
import com.samuelgularte.financeflow.budgeting.application.usecase.ProcessTextUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(TransactionControllerTest.TestConfig.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessTextUseCase processTextUseCase;

    @MockitoBean
    private ProcessAudioUseCase processAudioUseCase;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private CustomUserDetails userDetails;
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        var user = new User("test", "test@email.com", "pass");
        user.setId(userId);
        userDetails = new CustomUserDetails(user);
    }

    @Nested
    @DisplayName("POST /transactions/audio")
    class PostAudio {

        @Test
        @DisplayName("should return 200 with success message")
        void shouldReturnSuccessMessage() throws Exception {
            when(processAudioUseCase.execute(any(), eq(userId))).thenReturn("Transação registrada via áudio!");

            var audioFile = new MockMultipartFile("file", "audio.mp3", "audio/mpeg", "fake-audio".getBytes());

            mockMvc.perform(multipart("/transactions/audio")
                            .file(audioFile)
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Transação registrada via áudio!"));
        }
    }

    @Nested
    @DisplayName("POST /transactions/text")
    class PostText {

        @Test
        @DisplayName("should return 200 with success message")
        void shouldReturnSuccessMessage() throws Exception {
            when(processTextUseCase.execute("Gastei 50 reais", userId)).thenReturn("Transação registrada!");

            mockMvc.perform(post("/transactions/text")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\":\"Gastei 50 reais\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Transação registrada!"));
        }

        @Test
        @DisplayName("should return 400 when text is blank")
        void shouldReturnBadRequestWhenTextBlank() throws Exception {
            mockMvc.perform(post("/transactions/text")
                            .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"text\":\"\"}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
