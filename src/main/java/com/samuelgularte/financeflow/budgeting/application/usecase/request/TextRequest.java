package com.samuelgularte.financeflow.budgeting.application.usecase.request;

import jakarta.validation.constraints.NotBlank;

public record TextRequest(@NotBlank String text) {}
