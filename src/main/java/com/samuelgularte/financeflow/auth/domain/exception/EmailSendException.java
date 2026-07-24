package com.samuelgularte.financeflow.auth.domain.exception;

public class EmailSendException extends RuntimeException {
    public EmailSendException(String email) {
        super("Failed to send email to " + email);
    }
}
