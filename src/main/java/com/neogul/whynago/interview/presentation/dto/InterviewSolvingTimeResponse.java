package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.interview.service.dto.InterviewSolvingTimeResult;

// 점수에 시간이 얼마나 반영됐는지. 서술형 채점 응답과 형식이 같다.
public record InterviewSolvingTimeResponse(
        int elapsedSeconds,
        int averageSeconds,
        ElapsedPace pace,
        int scoreAdjustment
) {

    static InterviewSolvingTimeResponse from(InterviewSolvingTimeResult result) {
        return new InterviewSolvingTimeResponse(
                result.elapsedSeconds(), result.averageSeconds(), result.pace(), result.scoreAdjustment());
    }
}
