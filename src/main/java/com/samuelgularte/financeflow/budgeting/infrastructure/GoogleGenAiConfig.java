package com.samuelgularte.financeflow.budgeting.infrastructure;

import com.google.genai.Client;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "GEMINI_API_KEY", matchIfMissing = false)
class GoogleGenAiConfig {

    @Bean
    ChatModel chatModel() {
        var client = Client.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();

        var options = GoogleGenAiChatOptions.builder()
                .model("gemini-3.1-flash-lite")
                .temperature(0.5)
                .includeThoughts(true)
                .build();

        return GoogleGenAiChatModel.builder()
                .genAiClient(client)
                .options(options)
                .observationRegistry(ObservationRegistry.create())
                .build();
    }

    @Bean
    ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
