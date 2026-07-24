package com.samuelgularte.financeflow.auth.application.usecase.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank
    private String token;
}
