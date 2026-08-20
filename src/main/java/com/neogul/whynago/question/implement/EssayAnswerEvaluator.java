package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.EssayGradingTarget;
import com.neogul.whynago.question.domain.SolvingTime;
import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import com.neogul.whynago.question.implement.dto.RubricGrading;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EssayAnswerEvaluator {

    private static final int MAX_TURNS = 3;
    private static final int PASS_THRESHOLD = 7;
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 10;

    private final EssayAiClient essayAiClient;
    private final RubricGradingResolver rubricGradingResolver;

    // target의 루브릭은 본 질문 턴에서만 쓴다. 꼬리질문은 세션마다 AI가 만들어 Question이 없고
    // 따라서 채점 기준도 없다. 판별은 대화 이력의 완료 턴 수로 한다.
    public EssayEvaluation evaluate(String conversationId, EssayGradingTarget target, EssayGradingMode mode) {
        int completedTurns = essayAiClient.completedTurns(conversationId);
        boolean lastTurn = completedTurns >= MAX_TURNS - 1;
        // 대화 이력이 비어 있으면 본질문이다. ChatMemory는 프로세스 메모리라 재시작·다중 인스턴스에서
        // 0으로 리셋되는데, 그때는 꼬리질문을 본질문으로 오인하는 쪽이 낫다 — 반대로 오인하면
        // 본질문 판정이 조용히 버려져 사용자에게 숙련도가 아예 남지 않는다.
        int turn = completedTurns + 1;
        EssayGradingTarget applied = completedTurns == 0 ? target : target.asFollowupTurn();

        GradeAndFollowupResult result =
                essayAiClient.gradeAndGenerateFollowup(conversationId, applied, !lastTurn, mode);

        if (lastTurn) {
            essayAiClient.clearSession(conversationId);
        }

        // 점수는 서버가 정한다. 루브릭 배점 합에 소요시간 가감을 얹는다.
        // 시간 가감을 AI에 맡기면 프롬프트에 준 시간 신호가 점수와 mastery에 두 번 먹는다.
        RubricGrading grading =
                rubricGradingResolver.resolve(applied.rubric(), result.criteriaResults(), result.score());
        SolvingTime solvingTime = applied.solvingTime();
        int score = clamp(grading.score() + solvingTime.scoreAdjustment());

        return new EssayEvaluation(
                result.feedback(),
                result.modelAnswer(),
                score,
                score >= PASS_THRESHOLD,
                result.followupQuestion(),
                result.mastery(),
                result.masteryReason(),
                grading.criteria(),
                solvingTime,
                turn
        );
    }

    private int clamp(int score) {
        return Math.clamp(score, MIN_SCORE, MAX_SCORE);
    }
}
