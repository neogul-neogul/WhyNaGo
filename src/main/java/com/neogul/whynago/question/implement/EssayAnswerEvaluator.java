package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EssayAnswerEvaluator {

    private static final int MAX_TURNS = 3;
    private static final int PASS_THRESHOLD = 7;

    private final EssayAiClient essayAiClient;

    public EssayEvaluation evaluate(String conversationId, String question, String answer, EssayGradingMode mode) {
        boolean lastTurn = essayAiClient.completedTurns(conversationId) >= MAX_TURNS - 1;

        GradeAndFollowupResult result =
                essayAiClient.gradeAndGenerateFollowup(conversationId, question, answer, !lastTurn, mode);

        if (lastTurn) {
            essayAiClient.clearSession(conversationId);
        }

        return new EssayEvaluation(
                result.feedback(),
                result.modelAnswer(),
                result.score() >= PASS_THRESHOLD,
                result.followupQuestion()
        );
    }
}
