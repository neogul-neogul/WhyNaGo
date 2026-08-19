package com.neogul.whynago.question.implement.dto;

import com.neogul.whynago.common.domain.MasteryLevel;

// score는 AI가 항상 산출하므로 int다. 저장 경로에서 클라이언트가 중계하지 않았을 때만 null이 된다.
// mastery·masteryReason은 AI가 판정하지 못했으면 null이며, 그때는 숙련도를 기록하지 않는다.
public record EssayEvaluation(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        String followupQuestion,
        MasteryLevel mastery,
        String masteryReason
) {

    public boolean hasMastery() {
        return mastery != null;
    }
}
