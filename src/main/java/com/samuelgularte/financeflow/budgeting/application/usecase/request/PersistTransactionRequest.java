package com.samuelgularte.financeflow.budgeting.application.usecase.request;

import com.samuelgularte.financeflow.budgeting.domain.Category;
import org.springframework.ai.tool.annotation.ToolParam;

public record PersistTransactionRequest(
        @ToolParam(description = "Descricao do gasto")
        String description,

        @ToolParam(description = "Valor do gasto em centavos")
        long amount,

        @ToolParam(description = "Categoria da transacao")
        Category category,

        @ToolParam(description = "Data da transacao no formato ISO 8601 (yyyy-MM-dd'T'HH:mm:ss). Se nao foi informada, deixe vazio.")
        String createdAt
) {}
