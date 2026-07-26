package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.CreateEssaySolvedSessionResult;

public record CreateEssaySolvedSessionResponse(Long sessionId) {

    public static CreateEssaySolvedSessionResponse from(CreateEssaySolvedSessionResult result) {
        return new CreateEssaySolvedSessionResponse(result.sessionId());
    }
}
