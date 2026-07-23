package com.samuelgularte.financeflow.auth.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.LoginUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.SignUpUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.request.LoginRequest;
import com.samuelgularte.financeflow.auth.application.usecase.request.SignUpRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;

    public AuthController(SignUpUseCase signUpUseCase, LoginUseCase loginUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/public/signin")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest loginRequest){
        String message = loginUseCase.execute(loginRequest);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/public/signup")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
        String message = signUpUseCase.execute(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", message));
    }
}
