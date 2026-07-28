package com.samuelgularte.financeflow.auth.application.usecase.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank @Size(max = 50) String userName,
        @NotBlank @Size(max = 100) @Email String email,
        @NotBlank @Size(max = 120) String password
) {}
