package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.exception.SolvedSessionErrorCode;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedSessionReader {

    private final SolvedSessionRepository solvedSessionRepository;

    public SolvedSession read(Long solvedSessionId) {
        return solvedSessionRepository.findById(solvedSessionId)
                .orElseThrow(() -> new BusinessException(SolvedSessionErrorCode.SOLVED_SESSION_NOT_FOUND));
    }
}
