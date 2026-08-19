package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import java.util.List;

public record InterviewGradingResult(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        MasteryLevel mastery,
        String masteryReason,
        List<InterviewRubricCriterionResult> rubricCriteria,
        InterviewSolvingTimeResult solvingTime
) {

    public List<InterviewRubricCriterionResult> rubricCriteria() {
        return rubricCriteria == null ? List.of() : rubricCriteria;
    }
}
