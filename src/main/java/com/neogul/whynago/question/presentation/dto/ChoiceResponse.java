package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.ChoiceResult;

public record ChoiceResponse(
        Long id,
        String content,
        int sequence,
        String explanation,
        Long relatedQuestionId
) {

    static ChoiceResponse from(ChoiceResult result) {
        return new ChoiceResponse(
                result.id(),
                result.content(),
                result.sequence(),
                result.explanation(),
                result.relatedQuestionId()
        );
    }
}
