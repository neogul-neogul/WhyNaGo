package com.neogul.whynago.recommendation.presentation.dto;

import com.neogul.whynago.recommendation.service.dto.RecommendationResult;
import java.util.List;

public record RecommendationResponse(
        boolean personalized,
        boolean generated,
        List<RecommendedQuestionResponse> questions
) {

    public static RecommendationResponse from(RecommendationResult result) {
        return new RecommendationResponse(
                result.personalized(),
                result.generated(),
                result.questions().stream()
                        .map(RecommendedQuestionResponse::from)
                        .toList()
        );
    }
}
