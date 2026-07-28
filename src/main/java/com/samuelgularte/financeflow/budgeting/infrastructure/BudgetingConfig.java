package com.samuelgularte.financeflow.budgeting.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
class BudgetingConfig {

    @Bean
    String systemPrompt(@Value("classpath:budgeting/prompts/system-prompt.st") Resource resource) throws IOException {
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
