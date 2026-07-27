package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.EssaySessionResult;

public record EssaySessionResponse(String conversationId) {

    public static EssaySessionResponse from(EssaySessionResult result) {
        return new EssaySessionResponse(result.conversationId());
    }
}
