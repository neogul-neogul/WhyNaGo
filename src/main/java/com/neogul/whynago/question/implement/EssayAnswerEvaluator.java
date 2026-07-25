package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import com.neogul.whynago.question.implement.dto.EssayQnA;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.EssayTurn;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EssayAnswerEvaluator {

    private static final int MAX_TURNS = 3;

    private final EssayAiClient essayAiClient;

    public EssayEvaluation evaluate(List<EssayQnA> thread) {
        List<EssayTurn> aiThread = thread.stream()
                .map(qna -> new EssayTurn(qna.question(), qna.answer()))
                .toList();

        boolean generateFollowup = thread.size() < MAX_TURNS;
        GradeAndFollowupResult result = essayAiClient.gradeAndGenerateFollowup(aiThread, generateFollowup);
        return EssayEvaluation.from(result);
    }
}
