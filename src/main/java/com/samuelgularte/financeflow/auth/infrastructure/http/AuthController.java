package com.samuelgularte.financeflow.auth.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.ForgotPasswordUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.LoginUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.ResetPasswordUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.SignUpUseCase;
import com.samuelgularte.financeflow.auth.application.usecase.request.ForgotPasswordRequest;
import com.samuelgularte.financeflow.auth.application.usecase.request.LoginRequest;
import com.samuelgularte.financeflow.auth.application.usecase.request.ResetPasswordRequest;
import com.samuelgularte.financeflow.auth.application.usecase.request.SignUpRequest;
import com.samuelgularte.financeflow.auth.application.usecase.response.LoginResponse;
import com.samuelgularte.financeflow.auth.application.usecase.response.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    public AuthController(SignUpUseCase signUpUseCase,
                          LoginUseCase loginUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ResetPasswordUseCase resetPasswordUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.loginUseCase = loginUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    @PostMapping("/public/signin")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse response = loginUseCase.execute(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
        MessageResponse message = new MessageResponse(signUpUseCase.execute(signUpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        MessageResponse message = new MessageResponse(forgotPasswordUseCase.execute(request.getEmail()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        MessageResponse message = new MessageResponse(resetPasswordUseCase.execute(resetPasswordRequest.getToken(), resetPasswordRequest.getNewPassword()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }
}
