package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EssaySolvedReader {

    private final EssaySolvedRepository essaySolvedRepository;

    public List<EssaySolved> readOrdered(Long solvedSessionId) {
        return essaySolvedRepository.findBySolvedSessionIdOrderBySequence(solvedSessionId);
    }

    public List<Long> readSolvedQuestionIds(Long userId) {
        return essaySolvedRepository.findSolvedQuestionIds(userId);
    }
}
