package com.samuelgularte.financeflow.auth.infrastructure.email;

import com.samuelgularte.financeflow.auth.application.port.EmailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(classes = EmailServiceAsyncTest.AsyncTestConfig.class)
class EmailServiceAsyncTest {

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    @DisplayName("should not block the caller while SMTP is slow")
    void shouldNotBlockCallerWhileSmtpIsSlow() throws Exception {
        CountDownLatch smtpStarted = new CountDownLatch(1);
        CountDownLatch releaseSmtp = new CountDownLatch(1);
        blockSmtp(smtpStarted, releaseSmtp, new AtomicReference<>());

        CompletableFuture<Void> caller = CompletableFuture.runAsync(() ->
                emailSender.sendPasswordResetEmail("dest@example.com", "raw-token"));

        assertTrue(smtpStarted.await(2, TimeUnit.SECONDS),
                "SMTP send should start on a worker thread");
        assertTimeoutPreemptively(Duration.ofSeconds(1), () -> caller.join(),
                "sendPasswordResetEmail should return before SMTP finishes");

        releaseSmtp.countDown();
        caller.get(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("should send the email on a thread different from the caller")
    void shouldSendEmailOnSeparateThread() throws Exception {
        CountDownLatch smtpStarted = new CountDownLatch(1);
        CountDownLatch releaseSmtp = new CountDownLatch(1);
        AtomicReference<String> sendingThread = new AtomicReference<>();
        blockSmtp(smtpStarted, releaseSmtp, sendingThread);

        emailSender.sendPasswordResetEmail("dest@example.com", "raw-token");

        assertTrue(smtpStarted.await(2, TimeUnit.SECONDS),
                "SMTP send should start on a worker thread");

        String callerThread = Thread.currentThread().getName();
        String smtpThread = sendingThread.get();
        assertNotNull(smtpThread, "SMTP should execute on a worker thread");
        assertNotEquals(callerThread, smtpThread,
                "email should be sent on a thread different from the caller");

        releaseSmtp.countDown();
    }

    private void blockSmtp(CountDownLatch smtpStarted, CountDownLatch releaseSmtp,
                           AtomicReference<String> sendingThread) throws InterruptedException {
        doAnswer(invocation -> {
            sendingThread.set(Thread.currentThread().getName());
            smtpStarted.countDown();
            releaseSmtp.await(10, TimeUnit.SECONDS);
            return null;
        }).when(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Configuration
    @EnableAsync
    static class AsyncTestConfig {

        @Bean
        JavaMailSender javaMailSender() {
            return mock(JavaMailSender.class);
        }

        @Bean
        EmailSender emailSender(@Value("${spring.mail.username:test@example.com}") String from,
                                JavaMailSender javaMailSender) {
            return new EmailService(javaMailSender, from);
        }
    }
}
