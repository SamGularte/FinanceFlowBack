package com.samuelgularte.financeflow.auth.domain.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email: " + email + " already registered");
    }
}
