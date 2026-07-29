package com.samuelgularte.financeflow.budgeting.application.usecase.request;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateTransactionRequest(

        @Size(min = 1, max = 255)
        String description,

        @PositiveOrZero
        BigDecimal amount,

        Category category,

        LocalDateTime createdAt
) {
}
