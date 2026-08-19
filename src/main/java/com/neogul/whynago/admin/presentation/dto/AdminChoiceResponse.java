package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.AdminChoiceResult;

public record AdminChoiceResponse(
        Long id,
        int sequence,
        String content,
        boolean correct,
        String explanation,
        Long relatedQuestionId
) {

    public static AdminChoiceResponse from(AdminChoiceResult result) {
        return new AdminChoiceResponse(
                result.id(),
                result.sequence(),
                result.content(),
                result.correct(),
                result.explanation(),
                result.relatedQuestionId()
        );
    }
}
