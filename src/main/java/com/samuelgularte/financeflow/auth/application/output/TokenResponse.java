package com.samuelgularte.financeflow.auth.application.output;

public record TokenResponse(
        String token,
        String refreshToken,
        String type
) {
    public static TokenResponse of(String token, String refreshToken) {
        return new TokenResponse(token, refreshToken, "Bearer");
    }
}
