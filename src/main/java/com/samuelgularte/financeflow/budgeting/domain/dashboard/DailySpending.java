package com.samuelgularte.financeflow.budgeting.domain.dashboard;

import java.math.BigDecimal;

public record DailySpending(
        int day,
        BigDecimal total
) {
}
