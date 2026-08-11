package com.neogul.whynago.progress.presentation.dto;

import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.service.dto.ProgressDetailResult;
import com.neogul.whynago.question.domain.Category;
import java.util.Map;

public record ProgressResponse(
        int score,
        Tier tier,
        Tier nextTier,
        int scoreToNextTier,
        int totalQuestionCount,
        Map<Category, Integer> categoryQuestionCounts
) {

    public static ProgressResponse from(ProgressDetailResult result) {
        return new ProgressResponse(
                result.score(),
                result.tier(),
                result.nextTier(),
                result.scoreToNextTier(),
                result.totalQuestionCount(),
                result.categoryQuestionCounts()
        );
    }
}
