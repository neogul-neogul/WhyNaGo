package com.neogul.whynago.mastery.config;

import com.neogul.whynago.mastery.domain.MasteryPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 정책 객체는 Spring을 모르는 순수 도메인 클래스로 두고, 빈 등록만 여기서 담당한다.
// 판정 규칙은 기록(객관식 채점)과 조회(추천의 약점 프로필)가 함께 쓰므로 mastery가 갖는다.
@Configuration
public class MasteryPolicyConfig {

    @Bean
    public MasteryPolicy masteryPolicy() {
        return new MasteryPolicy();
    }
}
