package com.samuelgularte.financeflow.auth.application.usecase.response;

import lombok.Data;

@Data
public class TokenResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";

    public TokenResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
