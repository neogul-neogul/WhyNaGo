package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.RubricCriterionResult;

// 루브릭 항목 하나의 채점 결과. 사용자에게 어떤 기준을 맞추고 어떤 기준을 놓쳤는지 보여주는 값이다.
public record RubricCriterionResponse(String point, int weight, boolean met, String reason) {

    static RubricCriterionResponse from(RubricCriterionResult result) {
        return new RubricCriterionResponse(result.point(), result.weight(), result.met(), result.reason());
    }
}
