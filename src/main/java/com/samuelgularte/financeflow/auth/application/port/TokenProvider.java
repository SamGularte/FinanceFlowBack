package com.samuelgularte.financeflow.auth.application.port;

public interface TokenProvider {
    String generateTokenFromUsername(String username);
}
