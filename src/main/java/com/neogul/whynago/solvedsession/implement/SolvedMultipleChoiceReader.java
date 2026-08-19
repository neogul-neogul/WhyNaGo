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

    // 약점 프로필은 세션 단위가 아니라 사용자의 전체 이력을 본다.
    public List<SolvedMultipleChoice> readAllByUser(Long userId) {
        return solvedMultipleChoiceRepository.findByUserId(userId);
    }
}
