package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewGradingResult;

public record InterviewGradingResponse(String feedback, String modelAnswer, boolean isCorrect) {

    static InterviewGradingResponse from(InterviewGradingResult result) {
        return new InterviewGradingResponse(result.feedback(), result.modelAnswer(), result.isCorrect());
    }
}
