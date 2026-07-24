package com.samuelgularte.financeflow.auth.application.usecase;

import com.samuelgularte.financeflow.auth.application.usecase.request.LoginRequest;
import com.samuelgularte.financeflow.auth.application.usecase.response.LoginResponse;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidCredentialsException;
import com.samuelgularte.financeflow.auth.infrastructure.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public LoginUseCase(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public LoginResponse execute(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            String token = jwtUtils.generateTokenFromUsername(authentication.getName());
            return new LoginResponse(token);
        } catch (Exception e) {
            throw new InvalidCredentialsException();
        }
    }
}
