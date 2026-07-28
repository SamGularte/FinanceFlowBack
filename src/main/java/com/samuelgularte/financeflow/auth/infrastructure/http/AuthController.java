package com.samuelgularte.financeflow.auth.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.*;
import com.samuelgularte.financeflow.auth.application.usecase.request.*;
import com.samuelgularte.financeflow.auth.application.output.TokenResponse;
import com.samuelgularte.financeflow.auth.application.output.MessageResponse;
import com.samuelgularte.financeflow.auth.infrastructure.security.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
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
    private final CookieUtils cookieUtils;

    public AuthController(SignUpUseCase signUpUseCase,
                          LoginUseCase loginUseCase,
                          ForgotPasswordUseCase forgotPasswordUseCase,
                          ResetPasswordUseCase resetPasswordUseCase,
                          RefreshTokenUseCase refreshTokenUseCase,
                          LogoutUseCase logoutUseCase,
                          CookieUtils cookieUtils) {
        this.signUpUseCase = signUpUseCase;
        this.loginUseCase = loginUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.cookieUtils = cookieUtils;
    }

    @PostMapping("/public/signin")
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                                  HttpServletResponse response) {
        TokenResponse tokenResponse = loginUseCase.execute(loginRequest);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createAccessTokenCookie(tokenResponse.token()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createRefreshTokenCookie(tokenResponse.refreshToken()).toString());
        return ResponseEntity.ok(new MessageResponse("Authenticated"));
    }

    @PostMapping("/public/signup")
    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
        MessageResponse message = new MessageResponse(signUpUseCase.execute(signUpRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        MessageResponse message = new MessageResponse(forgotPasswordUseCase.execute(request.email()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest){
        MessageResponse message = new MessageResponse(resetPasswordUseCase.execute(resetPasswordRequest.token(), resetPasswordRequest.newPassword()));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }

    @PostMapping("/public/refresh-token")
    public ResponseEntity<MessageResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = cookieUtils.getRefreshTokenFromCookie(request);
        if (refreshTokenValue == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Refresh token is missing"));
        }
        TokenResponse tokenResponse = refreshTokenUseCase.execute(new RefreshTokenRequest(refreshTokenValue));
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createAccessTokenCookie(tokenResponse.token()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.createRefreshTokenCookie(tokenResponse.refreshToken()).toString());
        return ResponseEntity.ok(new MessageResponse("Token refreshed"));
    }

    @PostMapping("/public/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshTokenValue = cookieUtils.getRefreshTokenFromCookie(request);
        if (refreshTokenValue != null) {
            logoutUseCase.execute(new LogoutRequest(refreshTokenValue));
        }
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.clearAccessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtils.clearRefreshTokenCookie().toString());
        return ResponseEntity.ok(new MessageResponse("Logged out"));
    }
}
