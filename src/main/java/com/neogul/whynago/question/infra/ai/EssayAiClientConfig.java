package com.neogul.whynago.question.infra.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EssayAiClientConfig {

    @Bean
    @ConditionalOnProperty(name = "API_KEY")
    public EssayAiClient geminiEssayAiClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        return new GeminiEssayAiClient(chatClientBuilder, chatMemory);
    }

    @Bean
    @ConditionalOnMissingBean(EssayAiClient.class)
    public EssayAiClient mockEssayAiClient() {
        return new MockEssayAiClient();
    }
}
