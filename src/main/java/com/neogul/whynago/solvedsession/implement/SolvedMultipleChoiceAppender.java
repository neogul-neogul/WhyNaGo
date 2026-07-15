package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.implement.dto.ScoredQuestion;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedMultipleChoiceAppender {

    private final SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    public void appendAll(Long userId, Long solvedSessionId, List<ScoredQuestion> questions, LocalDateTime solvedAt) {
        List<SolvedMultipleChoice> items = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            ScoredQuestion question = questions.get(i);
            items.add(SolvedMultipleChoice.create(
                    solvedSessionId,
                    userId,
                    question.questionId(),
                    i == 0 ? ItemType.MAIN : ItemType.FOLLOWUP,
                    i + 1,
                    question.userChoiceId(),
                    question.answerChoiceId(),
                    question.correct(),
                    solvedAt
            ));
        }
        solvedMultipleChoiceRepository.saveAll(items);
    }
}
