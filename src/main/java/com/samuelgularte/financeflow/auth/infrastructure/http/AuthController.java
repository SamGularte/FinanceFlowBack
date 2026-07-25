package com.samuelgularte.financeflow.auth.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.*;
import com.samuelgularte.financeflow.auth.application.usecase.request.*;
import com.samuelgularte.financeflow.auth.application.usecase.response.TokenResponse;
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
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(SignUpUseCase signUpUseCase,
                          LoginUseCase loginUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ResetPasswordUseCase resetPasswordUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase) {
        this.signUpUseCase = signUpUseCase;
        this.loginUseCase = loginUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/public/signin")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        TokenResponse response = loginUseCase.execute(loginRequest);
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

    @PostMapping("/public/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        TokenResponse response = refreshTokenUseCase.execute(refreshTokenRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest logoutRequest){
        MessageResponse response = new MessageResponse(logoutUseCase.execute(logoutRequest));
        return ResponseEntity.ok(response);
    }
}
