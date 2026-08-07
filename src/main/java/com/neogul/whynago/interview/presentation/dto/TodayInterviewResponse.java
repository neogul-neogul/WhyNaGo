package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.TodayInterviewResult;

public record TodayInterviewResponse(String status, Long interviewId) {

    public static TodayInterviewResponse from(TodayInterviewResult result) {
        return new TodayInterviewResponse(result.status(), result.interviewId());
    }
}
