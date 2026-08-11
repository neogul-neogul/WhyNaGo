package com.neogul.whynago.progress.service.dto;

import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.implement.dto.UserProgressAggregate;
import com.neogul.whynago.question.domain.Category;
import java.util.Map;

public record ProgressDetailResult(
        int score,
        Tier tier,
        Tier nextTier,
        int scoreToNextTier,
        int totalQuestionCount,
        Map<Category, Integer> categoryQuestionCounts
) {

    public static ProgressDetailResult of(UserProgressAggregate aggregate, Tier tier) {
        return new ProgressDetailResult(
                aggregate.totalScore(),
                tier,
                tier.next().orElse(null),
                tier.scoreToNext(aggregate.totalScore()),
                aggregate.totalQuestionCount(),
                aggregate.categoryQuestionCounts()
        );
    }
}
