package com.neogul.whynago.question.infra.ai;

import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV4;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EssayAiClientConfig {

    @Bean
    // 프롬프트는 버전을 남겨 두고 빈만 갈아탄다. 판정 품질이 떨어지면 이전 버전으로 되돌린다.
    public EssayPrompt essayPrompt() {
        return new EssayPromptV4();
    }

    @Bean
    @ConditionalOnProperty(name = "whynago.ai.enabled", havingValue = "true")
    public EssayAiClient geminiEssayAiClient(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            EssayPrompt essayPrompt
    ) {
        return new GeminiEssayAiClient(chatClientBuilder, chatMemory, essayPrompt);
    }

    @Bean
    @ConditionalOnMissingBean(EssayAiClient.class)
    public EssayAiClient mockEssayAiClient() {
        return new MockEssayAiClient();
    }
}
