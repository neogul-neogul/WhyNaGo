package com.neogul.whynago.question.implement.dto;

import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;

public record EssayEvaluation(String feedback, String modelAnswer, String followupQuestion) {

    public static EssayEvaluation from(GradeAndFollowupResult result) {
        return new EssayEvaluation(result.feedback(), result.modelAnswer(), result.followupQuestion());
    }
}
