package com.samuelgularte.financeflow.auth.domain.exception;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() {
        super("Invalid or expired password reset token");
    }
}
