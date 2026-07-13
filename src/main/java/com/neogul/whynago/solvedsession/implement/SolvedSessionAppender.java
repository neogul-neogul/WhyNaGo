package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedSessionAppender {

    private final SolvedSessionRepository solvedSessionRepository;

    public SolvedSession append(SolvedSession solvedSession) {
        return solvedSessionRepository.save(solvedSession);
    }
}
