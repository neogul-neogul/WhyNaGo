package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.question.domain.SolvingTime;

public record InterviewSolvingTimeResult(
        int elapsedSeconds,
        int averageSeconds,
        ElapsedPace pace,
        int scoreAdjustment
) {

    public static InterviewSolvingTimeResult from(SolvingTime solvingTime) {
        return new InterviewSolvingTimeResult(
                solvingTime.elapsedSeconds(),
                solvingTime.baselineSeconds(),
                solvingTime.pace(),
                solvingTime.scoreAdjustment());
    }
}
