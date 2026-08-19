package com.neogul.whynago.recommendation.infra.ai.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import java.util.List;

// 프롬프트를 조립하는 데 필요한 재료다. 원본 풀이 이력은 넣지 않고 집계된 요약만 넘긴다.
public record EssayGenerationRequest(
        Category category,
        List<String> weakTags,
        List<String> allowedTags,
        Difficulty targetDifficulty,
        // 0.0~1.0. 숙련도 판정을 가중치로 환산해 평균한 값이며, 클수록 그 주제를 모른다는 뜻이다.
        double weaknessScore,
        // 정책이 이 주제를 고른 근거. 발문의 조준점으로 쓴다.
        String reason,
        // 기존 객관식 오답 해설. 오개념 카탈로그로 쓰되 정답 근거로 쓰지 않도록 프롬프트에서 라벨링한다.
        List<String> wrongExplanations,
        // 중복 생성을 막기 위한 네거티브 컨텍스트.
        List<String> existingTitles
) {
}
