package com.samuelgularte.financeflow.auth.infrastructure.email;

import com.samuelgularte.financeflow.auth.application.port.EmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final String from;

    public EmailService(JavaMailSender javaMailSender, @Value("${spring.mail.username}") String from){
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    public void sendPasswordResetEmail(String to, String resetToken){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Password reset request");
        message.setText("Click the link to reset your password: " + resetToken);
        javaMailSender.send(message);
    }
}
