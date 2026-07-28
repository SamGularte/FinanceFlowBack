package com.samuelgularte.financeflow.budgeting.application.output;

import com.samuelgularte.financeflow.budgeting.domain.TransactionPage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class TransactionPageMapper {

    public Page<TransactionOutput> toSpringPage(TransactionPage page) {
        var content = page.content().stream()
                .map(TransactionOutput::from)
                .toList();
        return new PageImpl<>(
                content,
                PageRequest.of(page.page(), page.size()),
                page.totalElements()
        );
    }
}
