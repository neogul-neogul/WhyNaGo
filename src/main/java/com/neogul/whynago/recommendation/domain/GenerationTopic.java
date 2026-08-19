package com.neogul.whynago.recommendation.domain;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import java.util.List;

// 서술형 문항 1개를 생성해 달라는 요청 단위다. LLM을 모르는 순수 도메인 모델이다.
public record GenerationTopic(
        Category category,
        List<String> tags,
        Difficulty targetDifficulty,
        double weaknessScore,
        String reason
) {
}
