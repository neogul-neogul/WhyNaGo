package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedMultipleChoiceReader {

    private final SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    public List<SolvedMultipleChoice> readOrdered(Long solvedSessionId) {
        return solvedMultipleChoiceRepository.findBySolvedSessionIdOrderBySequence(solvedSessionId);
    }

    public List<Long> readSolvedQuestionIds(Long userId) {
        return solvedMultipleChoiceRepository.findSolvedQuestionIds(userId);
    }
}
