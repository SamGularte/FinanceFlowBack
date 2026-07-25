package com.samuelgularte.financeflow.auth.infrastructure.security;

import com.samuelgularte.financeflow.auth.application.port.AuthenticationPort;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class SpringAuthenticationAdapter implements AuthenticationPort {

    private final AuthenticationManager authenticationManager;

    public SpringAuthenticationAdapter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String authenticate(String login, String password) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login, password));
            return authentication.getName();
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
    }
}
