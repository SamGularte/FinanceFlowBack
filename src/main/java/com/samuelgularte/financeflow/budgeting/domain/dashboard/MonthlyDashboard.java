package com.samuelgularte.financeflow.budgeting.domain.dashboard;

import com.samuelgularte.financeflow.budgeting.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyDashboard(
        BigDecimal totalSpent,
        int totalTransactions,
        BigDecimal averagePerTransaction,
        BigDecimal previousMonthTotal,
        List<CategorySpending> byCategory,
        List<DailySpending> dailyBreakdown,
        List<Transaction> topTransactions
) {
}
