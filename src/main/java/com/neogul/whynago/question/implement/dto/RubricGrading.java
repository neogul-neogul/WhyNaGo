package com.neogul.whynago.question.implement.dto;

import java.util.List;

// 루브릭 채점의 결과. score는 루브릭이 적용됐으면 충족 항목의 배점 합이고, 아니면 AI가 매긴 점수다.
// criteria는 루브릭이 적용되지 않았을 때 빈 리스트다.
public record RubricGrading(int score, List<RubricEvaluation> criteria) {

    public static RubricGrading withoutRubric(int aiScore) {
        return new RubricGrading(aiScore, List.of());
    }
}
