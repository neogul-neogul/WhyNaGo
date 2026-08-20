package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.question.implement.dto.RubricEvaluation;

public record InterviewRubricCriterionResult(String point, int weight, boolean met, String reason) {

    public static InterviewRubricCriterionResult from(RubricEvaluation evaluation) {
        return new InterviewRubricCriterionResult(
                evaluation.point(), evaluation.weight(), evaluation.met(), evaluation.reason());
    }
}
