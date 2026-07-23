package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.EssayAnswerResult;

public record EvaluateEssayAnswerResponse(GradingResponse grading, NextFollowupResponse nextFollowup) {

    public static EvaluateEssayAnswerResponse from(EssayAnswerResult result) {
        return new EvaluateEssayAnswerResponse(
                GradingResponse.from(result.grading()),
                result.nextFollowup() == null ? null : NextFollowupResponse.from(result.nextFollowup())
        );
    }
}
