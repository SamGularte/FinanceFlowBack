package com.samuelgularte.financeflow.auth.application.port;

public interface AuthenticationPort {
    String authenticate(String login, String password);
}
