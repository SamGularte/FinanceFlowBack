package com.samuelgularte.financeflow.auth.application.port;

public interface EmailSender {
    void sendPasswordResetEmail(String to, String resetToken);
}
