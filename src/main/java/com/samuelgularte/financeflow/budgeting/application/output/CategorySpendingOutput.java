package com.samuelgularte.financeflow.budgeting.application.output;

import java.math.BigDecimal;

public record CategorySpendingOutput(
        String category,
        BigDecimal total,
        double percentage
) {
}
