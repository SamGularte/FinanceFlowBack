package com.samuelgularte.financeflow.auth.infrastructure.email;

import com.samuelgularte.financeflow.auth.application.port.EmailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

@SpringJUnitConfig(classes = EmailServiceAsyncTest.AsyncTestConfig.class)
class EmailServiceAsyncTest {

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Test
    @DisplayName("should not block the caller while the Brevo API is slow")
    void shouldNotBlockCallerWhileBrevoIsSlow() throws Exception {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        CountDownLatch brevoStarted = new CountDownLatch(1);
        CountDownLatch releaseBrevo = new CountDownLatch(1);
        expectPost(server, brevoStarted, releaseBrevo, new AtomicReference<>());

        CompletableFuture<Void> caller = CompletableFuture.runAsync(() ->
                emailSender.sendPasswordResetEmail("dest@example.com", "raw-token"));

        assertTrue(brevoStarted.await(2, TimeUnit.SECONDS),
                "Brevo send should start on a worker thread");
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> caller.join(),
                "sendPasswordResetEmail should return before the Brevo call finishes");

        releaseBrevo.countDown();
        caller.get(5, TimeUnit.SECONDS);
        server.verify();
    }

    @Test
    @DisplayName("should send the email on a thread different from the caller")
    void shouldSendEmailOnSeparateThread() throws Exception {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        CountDownLatch brevoStarted = new CountDownLatch(1);
        CountDownLatch releaseBrevo = new CountDownLatch(1);
        AtomicReference<String> sendingThread = new AtomicReference<>();
        expectPost(server, brevoStarted, releaseBrevo, sendingThread);

        emailSender.sendPasswordResetEmail("dest@example.com", "raw-token");

        assertTrue(brevoStarted.await(2, TimeUnit.SECONDS),
                "Brevo send should start on a worker thread");

        String callerThread = Thread.currentThread().getName();
        String brevoThread = sendingThread.get();
        assertNotNull(brevoThread, "Brevo should execute on a worker thread");
        assertNotEquals(callerThread, brevoThread,
                "email should be sent on a thread different from the caller");

        releaseBrevo.countDown();
        server.verify();
    }

    private void expectPost(MockRestServiceServer server, CountDownLatch brevoStarted,
                            CountDownLatch releaseBrevo, AtomicReference<String> sendingThread)
            throws InterruptedException {
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(request -> {
                    sendingThread.set(Thread.currentThread().getName());
                    brevoStarted.countDown();
                    try {
                        releaseBrevo.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                    return MockRestResponseCreators.withStatus(HttpStatus.CREATED).createResponse(request);
                });
    }

    @Configuration
    @EnableAsync
    static class AsyncTestConfig {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }

        @Bean
        EmailSender emailSender(RestClient.Builder restClientBuilder,
                                @Value("${brevo.url:https://api.brevo.com/v3/smtp/email}") String url,
                                @Value("${brevo.api-key:test-key}") String apiKey,
                                @Value("${brevo.sender.name:financeflow}") String senderName,
                                @Value("${brevo.sender.email:test@example.com}") String senderEmail) {
            return new EmailService(restClientBuilder, url, apiKey, senderName, senderEmail);
        }
    }
}