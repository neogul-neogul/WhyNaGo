package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.question.service.dto.GradingResult;

public record GradingResponse(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        // AI가 판정하지 못하면 null이다. 클라이언트는 없을 수 있다고 보고 다뤄야 한다.
        MasteryLevel mastery,
        String masteryReason
) {

    static GradingResponse from(GradingResult result) {
        return new GradingResponse(
                result.feedback(),
                result.modelAnswer(),
                result.score(),
                result.isCorrect(),
                result.mastery(),
                result.masteryReason()
        );
    }
}
