package com.neogul.whynago.question.infra.ai;

import com.neogul.whynago.common.domain.MasteryLevel;

// mastery·masteryReason은 v4부터 채워진다. 이전 버전 프롬프트나 응답 누락 시 null이며,
// 그때는 숙련도를 기록하지 않고 채점만 진행한다.
public record GradeAndFollowupResult(
        String feedback,
        String modelAnswer,
        int score,
        String followupQuestion,
        MasteryLevel mastery,
        String masteryReason
) {
}
