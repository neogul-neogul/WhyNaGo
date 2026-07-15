package com.neogul.whynago.solvedsession.service.dto;

import com.neogul.whynago.solvedsession.domain.SolvedSession;

public record CreateSolvedSessionResult(Long sessionId) {

    public static CreateSolvedSessionResult from(SolvedSession solvedSession) {
        return new CreateSolvedSessionResult(solvedSession.getId());
    }
}
