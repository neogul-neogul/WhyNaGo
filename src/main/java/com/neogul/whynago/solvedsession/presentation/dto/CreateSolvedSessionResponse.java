package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.CreateSolvedSessionResult;

public record CreateSolvedSessionResponse(Long sessionId) {

    public static CreateSolvedSessionResponse from(CreateSolvedSessionResult result) {
        return new CreateSolvedSessionResponse(result.sessionId());
    }
}
