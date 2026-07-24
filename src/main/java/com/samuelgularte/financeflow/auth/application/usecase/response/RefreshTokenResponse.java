package com.samuelgularte.financeflow.auth.application.usecase.response;

import lombok.Data;

@Data
public class RefreshTokenResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";

    public RefreshTokenResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
