package com.neogul.whynago.recommendation.service.dto;

import java.util.List;

// personalized: 약점 프로필을 근거로 골랐는지 여부. 콜드스타트면 false다.
// generated: 이번 응답에 AI가 새로 만든 문항이 하나라도 포함됐는지 여부.
public record RecommendationResult(
        boolean personalized,
        boolean generated,
        List<RecommendedQuestionResult> questions
) {

    public static RecommendationResult of(List<RecommendedQuestionResult> questions, boolean personalized) {
        boolean generated = questions.stream().anyMatch(RecommendedQuestionResult::generated);
        return new RecommendationResult(personalized, generated, questions);
    }
}
