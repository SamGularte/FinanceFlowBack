package com.samuelgularte.financeflow.budgeting.application.input;

import jakarta.validation.constraints.NotBlank;

public record TextRequest(@NotBlank String text) {}
