package com.samuelgularte.financeflow.auth.infrastructure.email;

import com.samuelgularte.financeflow.auth.application.port.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailService implements EmailSender {

    private final RestClient.Builder restClientBuilder;
    private final String url;
    private final String apiKey;
    private final String senderName;
    private final String senderEmail;

    public EmailService(RestClient.Builder restClientBuilder,
                        @Value("${brevo.url}") String url,
                        @Value("${brevo.api-key}") String apiKey,
                        @Value("${brevo.sender.name}") String senderName,
                        @Value("${brevo.sender.email}") String senderEmail) {
        this.restClientBuilder = restClientBuilder;
        this.url = url;
        this.apiKey = apiKey;
        this.senderName = senderName;
        this.senderEmail = senderEmail;
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            RestClient client = restClientBuilder
                    .baseUrl(url)
                    .defaultHeader("api-key", apiKey)
                    .build();

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", "Password reset request",
                    "textContent", "Click the link to reset your password: " + resetToken
            );
            client.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to send password reset email to {} via Brevo", to, e);
        }
    }
}