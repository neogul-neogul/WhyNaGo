package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.interview.service.dto.InterviewGradingResult;

public record InterviewGradingResponse(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        // AI가 판정하지 못하면 null이다.
        MasteryLevel mastery,
        String masteryReason
) {

    static InterviewGradingResponse from(InterviewGradingResult result) {
        return new InterviewGradingResponse(
                result.feedback(),
                result.modelAnswer(),
                result.score(),
                result.isCorrect(),
                result.mastery(),
                result.masteryReason()
        );
    }
}
