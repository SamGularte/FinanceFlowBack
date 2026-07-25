package com.samuelgularte.financeflow.auth.application.port;

public interface PasswordEncoderPort {
    String encode(String rawPassword);
}
