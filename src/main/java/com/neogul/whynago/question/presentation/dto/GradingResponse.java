package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.question.service.dto.GradingResult;
import java.util.List;

public record GradingResponse(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        // AI가 판정하지 못하면 null이다. 클라이언트는 없을 수 있다고 보고 다뤄야 한다.
        MasteryLevel mastery,
        String masteryReason,
        // 루브릭이 없는 문항이나 꼬리질문 턴에서는 빈 배열이다. null로 내리지 않는다.
        List<RubricCriterionResponse> rubricCriteria,
        // 소요시간을 보고하지 않았거나 측정에 실패하면 null이다. 그때는 시간이 점수에 반영되지 않았다.
        SolvingTimeResponse solvingTime
) {

    static GradingResponse from(GradingResult result) {
        return new GradingResponse(
                result.feedback(),
                result.modelAnswer(),
                result.score(),
                result.isCorrect(),
                result.mastery(),
                result.masteryReason(),
                result.rubricCriteria().stream()
                        .map(RubricCriterionResponse::from)
                        .toList(),
                result.solvingTime() == null ? null : SolvingTimeResponse.from(result.solvingTime())
        );
    }
}
