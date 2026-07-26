package com.neogul.whynago.solvedsession.service.dto;

import com.neogul.whynago.solvedsession.domain.SolvedSession;

public record CreateEssaySolvedSessionResult(Long sessionId) {

    public static CreateEssaySolvedSessionResult from(SolvedSession solvedSession) {
        return new CreateEssaySolvedSessionResult(solvedSession.getId());
    }
}
