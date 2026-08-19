package com.neogul.whynago.question.implement;

import com.neogul.whynago.mastery.domain.MasterySource;
import com.neogul.whynago.mastery.service.MasteryService;
import com.neogul.whynago.mastery.service.dto.RecordMasteryCommand;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 채점 결과의 숙련도를 그 문항의 태그·카테고리에 연결해 기록한다.
// 태그 해석은 question 도메인이 하고, 기록은 mastery 도메인이 한다 — mastery는 question을 조회하지 않는다.
@Slf4j
@Component
@RequiredArgsConstructor
public class EssayMasteryRecorder {

    private final QuestionTagIdReader questionTagIdReader;
    private final MasteryService masteryService;

    public void record(Long userId, Question question, EssayEvaluation evaluation) {
        if (!evaluation.hasMastery()) {
            // v4 이전 프롬프트이거나 AI가 판정을 빠뜨린 경우다. 채점은 이미 끝났으므로 기록만 건너뛴다.
            log.warn("숙련도 판정이 없어 기록하지 않는다 - userId={}, questionId={}", userId, question.getId());
            return;
        }

        List<Long> tagIds = questionTagIdReader.readTagIds(question.getId());
        masteryService.record(new RecordMasteryCommand(
                userId,
                question.getId(),
                question.getCategory(),
                tagIds,
                evaluation.mastery(),
                evaluation.masteryReason(),
                MasterySource.AI_ESSAY
        ));
    }
}
