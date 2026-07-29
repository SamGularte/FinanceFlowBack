package com.samuelgularte.financeflow.budgeting.application.output;

import com.samuelgularte.financeflow.budgeting.domain.dashboard.MonthlyDashboard;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyDashboardOutput(
        BigDecimal totalSpent,
        int totalTransactions,
        BigDecimal averagePerTransaction,
        BigDecimal previousMonthTotal,
        List<CategorySpendingOutput> byCategory,
        List<DailySpendingOutput> dailyBreakdown,
        List<TransactionOutput> topTransactions
) {
    public static MonthlyDashboardOutput from(MonthlyDashboard dashboard) {
        return new MonthlyDashboardOutput(
                dashboard.totalSpent(),
                dashboard.totalTransactions(),
                dashboard.averagePerTransaction(),
                dashboard.previousMonthTotal(),
                dashboard.byCategory().stream()
                        .map(cs -> new CategorySpendingOutput(
                                cs.category().name(),
                                cs.total(),
                                cs.percentage()
                        ))
                        .toList(),
                dashboard.dailyBreakdown().stream()
                        .map(ds -> new DailySpendingOutput(ds.day(), ds.total()))
                        .toList(),
                dashboard.topTransactions().stream()
                        .map(TransactionOutput::from)
                        .toList()
        );
    }
}
