package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.question.implement.dto.EssayEvaluation;

public record EssayAnswerResult(GradingResult grading, NextFollowupResult nextFollowup) {

    public static EssayAnswerResult from(EssayEvaluation evaluation) {
        NextFollowupResult nextFollowup = evaluation.followupQuestion() == null
                ? null
                : new NextFollowupResult(evaluation.followupQuestion());
        return new EssayAnswerResult(
                new GradingResult(evaluation.feedback(), evaluation.modelAnswer()),
                nextFollowup
        );
    }
}
