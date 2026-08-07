package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.AnswerInterviewResult;

public record AnswerInterviewResponse(InterviewGradingResponse grading, InterviewFollowupResponse nextFollowup) {

    public static AnswerInterviewResponse from(AnswerInterviewResult result) {
        return new AnswerInterviewResponse(
                InterviewGradingResponse.from(result.grading()),
                result.nextFollowup() == null ? null : InterviewFollowupResponse.from(result.nextFollowup())
        );
    }
}
