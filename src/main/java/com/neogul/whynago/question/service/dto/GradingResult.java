package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import java.util.List;

public record GradingResult(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        MasteryLevel mastery,
        String masteryReason,
        List<RubricCriterionResult> rubricCriteria,
        SolvingTimeResult solvingTime
) {

    public List<RubricCriterionResult> rubricCriteria() {
        return rubricCriteria == null ? List.of() : rubricCriteria;
    }
}
