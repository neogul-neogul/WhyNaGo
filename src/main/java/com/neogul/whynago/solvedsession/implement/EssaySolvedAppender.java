package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.implement.dto.EssaySolvedPayload;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EssaySolvedAppender {

    private final EssaySolvedRepository essaySolvedRepository;

    public void appendAll(Long userId, Long solvedSessionId, List<EssaySolvedPayload> items, LocalDateTime solvedAt) {
        List<EssaySolved> entities = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            EssaySolvedPayload item = items.get(i);
            entities.add(EssaySolved.create(
                    solvedSessionId,
                    userId,
                    i == 0 ? ItemType.MAIN : ItemType.FOLLOWUP,
                    i + 1,
                    item.questionId(),
                    item.questionText(),
                    item.userAnswer(),
                    item.feedback(),
                    item.modelAnswer(),
                    item.isCorrect(),
                    item.score(),
                    item.elapsedSeconds(),
                    solvedAt
            ));
        }
        essaySolvedRepository.saveAll(entities);
    }
}
