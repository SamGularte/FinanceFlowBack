package com.samuelgularte.financeflow.auth.application.usecase.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
        @NotBlank String refreshToken
) {}
