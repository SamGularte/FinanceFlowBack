package com.samuelgularte.financeflow.budgeting.domain.dashboard;

import com.samuelgularte.financeflow.budgeting.domain.Category;

import java.math.BigDecimal;

public record CategorySpending(
        Category category,
        BigDecimal total,
        double percentage
) {
}
