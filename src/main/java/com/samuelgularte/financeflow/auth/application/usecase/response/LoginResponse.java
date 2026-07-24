package com.samuelgularte.financeflow.auth.application.usecase.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String type = "Bearer";

    public LoginResponse(String token) {
        this.token = token;
    }
}
