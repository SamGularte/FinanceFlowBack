package com.samuelgularte.financeflow.auth.application.usecase.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 6, max = 120) String newPassword
) {}
