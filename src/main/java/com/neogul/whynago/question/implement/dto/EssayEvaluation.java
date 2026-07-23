package com.neogul.whynago.question.implement.dto;

import com.neogul.whynago.question.infra.ai.GeneratedFollowup;
import com.neogul.whynago.question.infra.ai.GradedAnswer;

public record EssayEvaluation(String feedback, String modelAnswer, String followupQuestion) {

    public static EssayEvaluation of(GradedAnswer graded, GeneratedFollowup followup) {
        return new EssayEvaluation(
                graded.feedback(),
                graded.modelAnswer(),
                followup == null ? null : followup.question()
        );
    }
}
