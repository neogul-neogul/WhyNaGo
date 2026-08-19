package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.question.implement.dto.RubricEvaluation;

public record RubricCriterionResult(String point, int weight, boolean met, String reason) {

    public static RubricCriterionResult from(RubricEvaluation evaluation) {
        return new RubricCriterionResult(
                evaluation.point(), evaluation.weight(), evaluation.met(), evaluation.reason());
    }
}
