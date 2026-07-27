package com.samuelgularte.financeflow.budgeting.application.input;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

public record PersistTransactionInput(
        @ToolParam(description = "Descricao do gasto")
        String description,

        @ToolParam(description = "Valor do gasto em centavos")
        long amount,

        @ToolParam(description = "Categoria da transacao")
        Category category
) {}
