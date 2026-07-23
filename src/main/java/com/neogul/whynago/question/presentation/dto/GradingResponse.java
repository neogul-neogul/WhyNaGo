package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.GradingResult;

public record GradingResponse(String feedback, String modelAnswer) {

    static GradingResponse from(GradingResult result) {
        return new GradingResponse(result.feedback(), result.modelAnswer());
    }
}
