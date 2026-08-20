package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewRubricCriterionResult;

// 루브릭 항목 하나의 채점 결과. 지원자가 어떤 기준을 맞추고 어떤 기준을 놓쳤는지 보여주는 값이다.
public record InterviewRubricCriterionResponse(String point, int weight, boolean met, String reason) {

    static InterviewRubricCriterionResponse from(InterviewRubricCriterionResult result) {
        return new InterviewRubricCriterionResponse(
                result.point(), result.weight(), result.met(), result.reason());
    }
}
