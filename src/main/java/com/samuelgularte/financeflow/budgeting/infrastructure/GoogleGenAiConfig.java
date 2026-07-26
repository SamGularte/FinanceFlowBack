package com.samuelgularte.financeflow.budgeting.infrastructure;

import com.google.genai.Client;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@ConditionalOnProperty("GEMINI_API_KEY")
class GoogleGenAiConfig {

    @Bean
    ChatModel chatModel() {
        var client = Client.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();

        var options = GoogleGenAiChatOptions.builder()
                .model("gemini-3.5-flash")
                .temperature(0.5)
                .build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(client)
                .defaultOptions(options)
                .toolCallingManager(ToolCallingManager.builder().build())
                .retryTemplate(new RetryTemplate())
                .observationRegistry(ObservationRegistry.create())
                .build();
    }
}
