package com.samuelgularte.financeflow.budgeting.application.output;

import java.math.BigDecimal;

public record DailySpendingOutput(
        int day,
        BigDecimal total
) {
}
