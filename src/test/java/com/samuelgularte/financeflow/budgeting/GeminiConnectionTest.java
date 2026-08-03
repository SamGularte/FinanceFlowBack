package com.samuelgularte.financeflow.budgeting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.app.jwtSecret=test",
    "brevo.api-key=test-key",
    "brevo.url=https://api.brevo.com/v3/smtp/email",
    "brevo.sender.name=financeflow",
    "brevo.sender.email=test@example.com"
})
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class GeminiConnectionTest {

    @Autowired
    private ChatModel chatModel;

    @Test
    @DisplayName("should respond to a simple prompt")
    void testConnection() {
        String response = chatModel.call(new Prompt("Responda apenas: OK")).getResult().getOutput().getText();
        assertNotNull(response);
        System.out.println(">>> Gemini: " + response);
    }
}
