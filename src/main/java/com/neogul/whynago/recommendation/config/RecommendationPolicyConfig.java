package com.neogul.whynago.recommendation.config;

import com.neogul.whynago.recommendation.domain.GeneratedEssayValidator;
import com.neogul.whynago.recommendation.domain.RecommendationTopicPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 정책 객체는 Spring을 모르는 순수 도메인 클래스로 두고, 빈 등록만 여기서 담당한다.
@Configuration
public class RecommendationPolicyConfig {

    @Bean
    public RecommendationTopicPolicy recommendationTopicPolicy() {
        return new RecommendationTopicPolicy();
    }

    @Bean
    public GeneratedEssayValidator generatedEssayValidator() {
        return new GeneratedEssayValidator();
    }
}
