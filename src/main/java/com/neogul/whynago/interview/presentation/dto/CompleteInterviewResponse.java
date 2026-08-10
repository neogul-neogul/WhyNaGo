package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.CompleteInterviewResult;

public record CompleteInterviewResponse(Long interviewId, Long solvedSessionId) {

    public static CompleteInterviewResponse from(CompleteInterviewResult result) {
        return new CompleteInterviewResponse(result.interviewId(), result.solvedSessionId());
    }
}
