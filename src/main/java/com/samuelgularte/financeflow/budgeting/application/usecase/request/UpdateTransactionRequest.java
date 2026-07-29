package com.samuelgularte.financeflow.budgeting.application.usecase.request;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateTransactionRequest(

        @Size(min = 1, max = 255)
        String description,

        @PositiveOrZero
        Long amount,

        Category category
) {
}
