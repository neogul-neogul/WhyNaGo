package com.neogul.whynago.progress.service.dto;

import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.implement.dto.CategoryProgress;
import com.neogul.whynago.progress.implement.dto.UserProgressAggregate;
import java.util.Arrays;
import java.util.List;

public record ProgressDetailResult(
        int score,
        Tier tier,
        Tier nextTier,
        int scoreToNextTier,
        int totalQuestionCount,
        List<CategoryProgressResult> categories,
        List<TierRange> tiers,
        int maxScore
) {

    public static ProgressDetailResult of(
            UserProgressAggregate aggregate,
            Tier tier,
            List<CategoryProgress> categories
    ) {
        return new ProgressDetailResult(
                aggregate.totalScore(),
                tier,
                tier.next().orElse(null),
                tier.scoreToNext(aggregate.totalScore()),
                aggregate.totalQuestionCount(),
                categories.stream().map(CategoryProgressResult::from).toList(),
                Arrays.stream(Tier.values()).map(TierRange::from).toList(),
                Tier.MAX_SCORE
        );
    }
}
