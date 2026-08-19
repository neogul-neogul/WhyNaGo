package com.neogul.whynago.question.infra.ai;

import com.neogul.whynago.common.domain.MasteryLevel;
import java.util.List;

// mastery·masteryReason은 v4부터 채워진다. 이전 버전 프롬프트나 응답 누락 시 null이며,
// 그때는 숙련도를 기록하지 않고 채점만 진행한다.
//
// criteriaResults는 v5부터, 그중에서도 루브릭이 있는 문항의 본 질문 턴에만 채워진다.
// 꼬리질문 턴이나 루브릭이 없는 문항에서는 비어 있고, 그때는 score를 그대로 쓴다.
public record GradeAndFollowupResult(
        String feedback,
        String modelAnswer,
        int score,
        String followupQuestion,
        MasteryLevel mastery,
        String masteryReason,
        List<CriterionGrading> criteriaResults
) {

    public List<CriterionGrading> criteriaResults() {
        return criteriaResults == null ? List.of() : criteriaResults;
    }
}
