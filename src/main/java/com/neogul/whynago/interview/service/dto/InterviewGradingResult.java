package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.common.domain.MasteryLevel;

public record InterviewGradingResult(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        MasteryLevel mastery,
        String masteryReason
) {
}
