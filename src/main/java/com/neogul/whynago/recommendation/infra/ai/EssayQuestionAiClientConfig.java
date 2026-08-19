package com.neogul.whynago.recommendation.infra.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class EssayQuestionAiClientConfig {

    // 프롬프트를 클래스패스 리소스로 두어 재컴파일 없이 문구를 고칠 수 있게 한다.
    @Value("classpath:prompts/essay-question-generation.st")
    private Resource essayQuestionGenerationPrompt;

    @Bean
    @ConditionalOnProperty(name = "whynago.ai.enabled", havingValue = "true")
    public EssayQuestionAiClient geminiEssayQuestionAiClient(ChatClient.Builder chatClientBuilder) {
        return new GeminiEssayQuestionAiClient(chatClientBuilder, essayQuestionGenerationPrompt);
    }

    @Bean
    @ConditionalOnMissingBean(EssayQuestionAiClient.class)
    public EssayQuestionAiClient mockEssayQuestionAiClient() {
        return new MockEssayQuestionAiClient();
    }
}
