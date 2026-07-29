package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.CategorySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.DailySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.MonthlyDashboard;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class GetMonthlyDashboardUseCase {

    private final TransactionRepository transactionRepository;

    public GetMonthlyDashboardUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public MonthlyDashboard execute(UUID userId, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        log.info("Building monthly dashboard for userId={}, year={}, month={}", userId, targetYear, targetMonth);

        BigDecimal totalSpent = transactionRepository.sumByUserIdAndMonth(userId, targetYear, targetMonth);
        long totalTransactions = transactionRepository.countByUserIdAndMonth(userId, targetYear, targetMonth);
        BigDecimal averagePerTransaction = totalTransactions > 0
                ? totalSpent.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<Category, BigDecimal> byCategoryRaw = transactionRepository.sumGroupByCategoryAndMonth(userId, targetYear, targetMonth);
        List<CategorySpending> byCategory = buildCategorySpendingList(byCategoryRaw, totalSpent);

        Map<Integer, BigDecimal> byDayRaw = transactionRepository.sumByDayAndMonth(userId, targetYear, targetMonth);
        List<DailySpending> dailyBreakdown = buildDailySpendingList(byDayRaw);

        List<Transaction> topTransactions = transactionRepository.findTopByUserIdAndMonth(userId, targetYear, targetMonth, 5);

        LocalDate previousMonthDate = now.withDayOfMonth(1).minusMonths(1);
        int prevYear = previousMonthDate.getYear();
        int prevMonth = previousMonthDate.getMonthValue();
        BigDecimal previousMonthTotal = transactionRepository.sumByUserIdAndMonth(userId, prevYear, prevMonth);

        return new MonthlyDashboard(
                totalSpent,
                (int) totalTransactions,
                averagePerTransaction,
                previousMonthTotal,
                byCategory,
                dailyBreakdown,
                topTransactions
        );
    }

    private List<CategorySpending> buildCategorySpendingList(Map<Category, BigDecimal> raw, BigDecimal totalSpent) {
        List<CategorySpending> list = new ArrayList<>();
        for (Map.Entry<Category, BigDecimal> entry : raw.entrySet()) {
            double percentage = totalSpent.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(BigDecimal.valueOf(100))
                        .divide(totalSpent, 2, RoundingMode.HALF_UP)
                        .doubleValue()
                    : 0.0;
            list.add(new CategorySpending(entry.getKey(), entry.getValue(), percentage));
        }
        return list;
    }

    private List<DailySpending> buildDailySpendingList(Map<Integer, BigDecimal> raw) {
        List<DailySpending> list = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : raw.entrySet()) {
            list.add(new DailySpending(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}
