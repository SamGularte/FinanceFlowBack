package com.samuelgularte.financeflow.auth.application.usecase.request;

import com.samuelgularte.financeflow.auth.domain.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(max = 120) @StrongPassword String newPassword
) {}
