package com.samuelgularte.financeflow.budgeting.application.usecase;

import com.samuelgularte.financeflow.budgeting.domain.MonthlyInsight;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.CategorySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.DailySpending;
import com.samuelgularte.financeflow.budgeting.domain.dashboard.MonthlyDashboard;
import com.samuelgularte.financeflow.budgeting.domain.repository.MonthlyInsightRepository;
import com.samuelgularte.financeflow.budgeting.infrastructure.i18n.Messages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class GenerateMonthlyInsightUseCase {

    private final MonthlyInsightRepository insightRepository;
    private final GetMonthlyDashboardUseCase getMonthlyDashboardUseCase;
    private final ChatClient chatClient;
    private final Messages messages;

    public GenerateMonthlyInsightUseCase(MonthlyInsightRepository insightRepository,
                                         GetMonthlyDashboardUseCase getMonthlyDashboardUseCase,
                                         ChatClient chatClient,
                                         Messages messages) {
        this.insightRepository = insightRepository;
        this.getMonthlyDashboardUseCase = getMonthlyDashboardUseCase;
        this.chatClient = chatClient;
        this.messages = messages;
    }

    public MonthlyInsight execute(UUID userId, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        log.info("Generating insight for userId={}, year={}, month={}", userId, targetYear, targetMonth);

        var existing = insightRepository.findByUserIdAndYearAndMonth(userId, targetYear, targetMonth);
        if (existing.isPresent()) {
            var insight = existing.get();
            boolean isClosed = insight.isClosedMonth(now);
            boolean reuse = isClosed
                    ? insight.isLastDayOfMonth()
                    : insight.wasGeneratedOnDate(now);

            if (reuse) {
                log.info("Reusing cached insight for {}-{} (closed={})", targetYear, targetMonth, isClosed);
                return insight;
            }
        }

        MonthlyDashboard dashboard = getMonthlyDashboardUseCase.execute(userId, targetYear, targetMonth);

        String content;
        if (dashboard.totalTransactions() < 5) {
            content = messages.get("insight.few-transactions");
        } else {
            String prompt = buildPrompt(dashboard);
            content = generateContent(prompt);
        }

        UUID id = existing.map(MonthlyInsight::id).orElseGet(UUID::randomUUID);
        var insight = new MonthlyInsight(
                id,
                userId,
                targetYear,
                targetMonth,
                content,
                LocalDateTime.now()
        );

        return insightRepository.save(insight);
    }

    private String buildPrompt(MonthlyDashboard dashboard) {
        var sb = new StringBuilder();

        BigDecimal prevTotal = dashboard.previousMonthTotal();
        String variacao;
        if (prevTotal.compareTo(BigDecimal.ZERO) > 0) {
            double percentual = dashboard.totalSpent().subtract(prevTotal)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(prevTotal, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            variacao = String.format("%+.1f%%", percentual);
        } else {
            variacao = "N/A (sem dados do mês anterior)";
        }

        sb.append("Dados do mês:\n");
        sb.append(String.format("- Total gasto: R$ %.2f (%s vs mês anterior)%n", dashboard.totalSpent(), variacao));
        sb.append(String.format("- Transações: %d (média de R$ %.2f cada)%n",
                dashboard.totalTransactions(), dashboard.averagePerTransaction()));

        if (!dashboard.byCategory().isEmpty()) {
            sb.append("\n- Gastos por categoria:\n");
            for (CategorySpending cat : dashboard.byCategory()) {
                sb.append(String.format("  • %s: R$ %.2f (%.1f%%)%n",
                        cat.category(), cat.total(), cat.percentage()));
            }
        }

        if (!dashboard.dailyBreakdown().isEmpty()) {
            sb.append("\n- Gastos por dia:\n");
            for (DailySpending day : dashboard.dailyBreakdown()) {
                sb.append(String.format("  • Dia %d: R$ %.2f%n", day.day(), day.total()));
            }
        }

        return sb.toString();
    }

    private String generateContent(String prompt) {
        String systemPrompt = """
                Você é um analista financeiro sênior. Sua função é gerar um relatório curto e objetivo sobre os gastos do usuário.

                Estrutura obrigatória do relatório (2-3 parágrafos):
                1. RESUMO DO MÊS: compare o total gasto com o mês anterior, destacando a variação percentual e a categoria de maior peso.
                2. PADRÕES RELEVANTES: mencione concentração em categorias, dias com pico de gastos, volume atípico de transações e identifique onde o usuário poderia reduzir ou cortar gastos se necessário.
                3. RECOMENDAÇÃO PRÁTICA: uma sugestão específica e acionável baseada nos dados apresentados.

                Tom: direto, sem rodeios. Use números concretos. Responda em português.""";
        return chatClient.prompt()
                .system(systemPrompt)
                .user(prompt)
                .call()
                .content();
    }
}
