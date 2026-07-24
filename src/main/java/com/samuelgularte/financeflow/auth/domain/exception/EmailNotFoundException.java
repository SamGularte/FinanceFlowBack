package com.samuelgularte.financeflow.auth.domain.exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String email) {
        super("Email: " + email + " not found");
    }
}
