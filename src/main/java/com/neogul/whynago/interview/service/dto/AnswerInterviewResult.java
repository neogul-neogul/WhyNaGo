package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.question.implement.dto.EssayEvaluation;

public record AnswerInterviewResult(InterviewGradingResult grading, InterviewFollowupResult nextFollowup) {

    public static AnswerInterviewResult from(EssayEvaluation evaluation) {
        InterviewFollowupResult nextFollowup = evaluation.followupQuestion() == null
                ? null
                : new InterviewFollowupResult(evaluation.followupQuestion());
        return new AnswerInterviewResult(
                new InterviewGradingResult(
                        evaluation.feedback(),
                        evaluation.modelAnswer(),
                        evaluation.score(),
                        evaluation.isCorrect(),
                        evaluation.mastery(),
                        evaluation.masteryReason(),
                        evaluation.rubricCriteria().stream()
                                .map(InterviewRubricCriterionResult::from)
                                .toList(),
                        evaluation.solvingTime().isMeasured()
                                ? InterviewSolvingTimeResult.from(evaluation.solvingTime())
                                : null
                ),
                nextFollowup
        );
    }
}
