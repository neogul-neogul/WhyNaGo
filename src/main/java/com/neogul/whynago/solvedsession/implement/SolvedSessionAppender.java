package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedSessionAppender {

    private final SolvedSessionRepository solvedSessionRepository;

    public SolvedSession append(Long userId, int totalCount, int correctCount, LocalDateTime solvedAt) {
        return solvedSessionRepository.save(SolvedSession.completed(
                userId,
                QuestionType.MULTIPLE_CHOICE,
                totalCount,
                correctCount,
                solvedAt
        ));
    }
}
