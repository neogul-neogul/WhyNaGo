package com.neogul.whynago.progress.service.dto;

import com.neogul.whynago.learningrecord.service.dto.StreakResult;
import com.neogul.whynago.progress.implement.dto.UserProgressAggregate;

public record ProgressSummaryResult(
        int cumulativeDays,
        int streakDays,
        int totalQuestionCount,
        int totalCorrectCount,
        int totalWrongCount,
        int completedInterviewCount
) {

    public static ProgressSummaryResult of(StreakResult streak, UserProgressAggregate aggregate, int completedInterviewCount) {
        return new ProgressSummaryResult(
                streak.cumulativeDays(),
                streak.streakDays(),
                aggregate.totalQuestionCount(),
                aggregate.totalCorrectCount(),
                aggregate.totalQuestionCount() - aggregate.totalCorrectCount(),
                completedInterviewCount
        );
    }
}
