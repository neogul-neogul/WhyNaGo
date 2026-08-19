package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.common.domain.MasteryLevel;

public record GradingResult(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        MasteryLevel mastery,
        String masteryReason
) {
}
