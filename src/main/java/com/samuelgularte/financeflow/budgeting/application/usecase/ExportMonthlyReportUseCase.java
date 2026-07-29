package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.application.output.ExportReportResult;
import com.samuelgularte.financeflow.budgeting.domain.Transaction;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.CategorySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.MonthlyDashboard;
import com.samuelgularte.financeflow.budgeting.domain.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ExportMonthlyReportUseCase {

    private final GetMonthlyDashboardUseCase getMonthlyDashboardUseCase;
    private final TransactionRepository transactionRepository;

    public ExportMonthlyReportUseCase(GetMonthlyDashboardUseCase getMonthlyDashboardUseCase,
                                      TransactionRepository transactionRepository) {
        this.getMonthlyDashboardUseCase = getMonthlyDashboardUseCase;
        this.transactionRepository = transactionRepository;
    }

    public ExportReportResult execute(UUID userId, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        log.info("Exporting monthly report for userId={}, year={}, month={}", userId, targetYear, targetMonth);

        MonthlyDashboard dashboard = getMonthlyDashboardUseCase.execute(userId, targetYear, targetMonth);
        List<Transaction> transactions = transactionRepository.findAllByUserIdAndMonth(userId, targetYear, targetMonth);

        StringBuilder csv = new StringBuilder();

        String monthName = LocalDate.of(targetYear, targetMonth, 1)
                .format(DateTimeFormatter.ofPattern("MMMM/yyyy"));
        csv.append("RELATORIO MENSAL - ").append(monthName).append('\n');
        csv.append("Total;").append(formatAmount(dashboard.totalSpent())).append('\n');
        csv.append("Transacoes;").append(dashboard.totalTransactions()).append('\n');
        csv.append("Media;").append(formatAmount(dashboard.averagePerTransaction())).append('\n');
        csv.append("Mes Anterior;").append(formatAmount(dashboard.previousMonthTotal())).append('\n');

        BigDecimal variation = calculateVariation(dashboard.previousMonthTotal(), dashboard.totalSpent());
        csv.append("Variacao;").append(formatAmount(variation)).append("%\n");
        csv.append('\n');

        csv.append("GASTOS POR CATEGORIA\n");
        csv.append("Categoria;Total;%\n");
        for (CategorySpending cs : dashboard.byCategory()) {
            csv.append(cs.category().name()).append(';')
                    .append(formatAmount(cs.total())).append(';')
                    .append(String.format("%.1f%%", cs.percentage())).append('\n');
        }
        csv.append('\n');

        csv.append("TRANSACOES\n");
        csv.append("Data;Descricao;Valor;Categoria\n");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Transaction tx : transactions) {
            csv.append(tx.createdAt().format(dateFormatter)).append(';')
                    .append(tx.description()).append(';')
                    .append(formatAmount(tx.amount())).append(';')
                    .append(tx.category().name()).append('\n');
        }

        return new ExportReportResult(csv.toString().getBytes(StandardCharsets.UTF_8), targetYear, targetMonth);
    }

    private String formatAmount(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString().replace(".", ",");
    }

    private BigDecimal calculateVariation(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : new BigDecimal("100.00");
        }
        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }
}
