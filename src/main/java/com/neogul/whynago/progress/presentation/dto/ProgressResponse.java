package com.neogul.whynago.progress.presentation.dto;

import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.service.dto.ProgressDetailResult;
import java.util.List;

public record ProgressResponse(
        int score,
        Tier tier,
        Tier nextTier,
        int scoreToNextTier,
        int totalQuestionCount,
        List<CategoryProgressResponse> categories,
        List<TierRangeResponse> tiers,
        int maxScore
) {

    public static ProgressResponse from(ProgressDetailResult result) {
        return new ProgressResponse(
                result.score(),
                result.tier(),
                result.nextTier(),
                result.scoreToNextTier(),
                result.totalQuestionCount(),
                result.categories().stream().map(CategoryProgressResponse::from).toList(),
                result.tiers().stream().map(TierRangeResponse::from).toList(),
                result.maxScore()
        );
    }
}
