package com.neogul.whynago.progress.presentation.dto;

import com.neogul.whynago.progress.service.dto.ProgressSummaryResult;

public record ProgressSummaryResponse(
        int cumulativeDays,
        int streakDays,
        int totalQuestionCount,
        int totalCorrectCount,
        int totalWrongCount,
        int completedInterviewCount
) {

    public static ProgressSummaryResponse from(ProgressSummaryResult result) {
        return new ProgressSummaryResponse(
                result.cumulativeDays(),
                result.streakDays(),
                result.totalQuestionCount(),
                result.totalCorrectCount(),
                result.totalWrongCount(),
                result.completedInterviewCount()
        );
    }
}
