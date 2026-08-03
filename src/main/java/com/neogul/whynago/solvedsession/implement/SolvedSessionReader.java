package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.exception.SolvedSessionErrorCode;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedSessionReader {

    private final SolvedSessionRepository solvedSessionRepository;

    public SolvedSession read(Long solvedSessionId) {
        return solvedSessionRepository.findById(solvedSessionId)
                .orElseThrow(() -> new BusinessException(SolvedSessionErrorCode.SOLVED_SESSION_NOT_FOUND));
    }

    public List<SolvedSession> readRecent(Long userId, int size) {
        return solvedSessionRepository.findByUserIdOrderBySolvedAtDesc(userId, PageRequest.of(0, Math.max(size, 1)));
    }

    public List<SolvedSession> readAll(Long userId) {
        return solvedSessionRepository.findByUserIdOrderBySolvedAtDesc(userId, Pageable.unpaged());
    }

    public List<SolvedSession> readBetween(Long userId, LocalDateTime from, LocalDateTime to) {
        return solvedSessionRepository.findByUserIdAndSolvedAtBetween(userId, from, to);
    }
}
