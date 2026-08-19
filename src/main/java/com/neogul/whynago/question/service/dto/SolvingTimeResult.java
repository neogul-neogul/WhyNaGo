package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.question.domain.SolvingTime;

public record SolvingTimeResult(int elapsedSeconds, int averageSeconds, ElapsedPace pace, int scoreAdjustment) {

    public static SolvingTimeResult from(SolvingTime solvingTime) {
        return new SolvingTimeResult(
                solvingTime.elapsedSeconds(),
                solvingTime.baselineSeconds(),
                solvingTime.pace(),
                solvingTime.scoreAdjustment());
    }
}
