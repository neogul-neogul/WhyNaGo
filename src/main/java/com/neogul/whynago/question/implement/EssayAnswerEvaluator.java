package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import com.neogul.whynago.question.implement.dto.EssayQnA;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.EssayTurn;
import com.neogul.whynago.question.infra.ai.GeneratedFollowup;
import com.neogul.whynago.question.infra.ai.GradedAnswer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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

        CompletableFuture<GradedAnswer> gradingFuture =
                CompletableFuture.supplyAsync(() -> essayAiClient.grade(aiThread));
        CompletableFuture<GeneratedFollowup> followupFuture = thread.size() < MAX_TURNS
                ? CompletableFuture.supplyAsync(() -> essayAiClient.generateFollowup(aiThread))
                : CompletableFuture.completedFuture(null);

        try {
            return EssayEvaluation.of(gradingFuture.join(), followupFuture.join());
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw e;
        }
    }
}
