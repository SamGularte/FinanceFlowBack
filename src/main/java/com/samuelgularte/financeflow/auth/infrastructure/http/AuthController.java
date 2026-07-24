package com.samuelgularte.financeflow.auth.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.ForgotPasswordUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.LoginUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.ResetPasswordResetUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.SignUpUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.request.ForgotPasswordRequest;
import com.samuelgularte.financeflow.auth.application.usecase.request.LoginRequest;
import com.samuelgularte.financeflow.auth.application.usecase.request.ResetPasswordRequest;
import com.samuelgularte.financeflow.auth.application.usecase.request.SignUpRequest;
import com.samuelgularte.financeflow.auth.application.usecase.response.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordResetUseCase resetPasswordResetUseCase;

    public AuthController(SignUpUseCase signUpUseCase,
                          LoginUseCase loginUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ResetPasswordResetUseCase resetPasswordResetUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.loginUseCase = loginUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordResetUseCase = resetPasswordResetUseCase;
    }

    @PostMapping("/public/signin")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse response = loginUseCase.execute(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/signup")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
        String message = signUpUseCase.execute(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", message));
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        String message = forgotPasswordUseCase.execute(request.getEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", message));
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        String message = resetPasswordResetUseCase.execute(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("message", message));
    }
}
