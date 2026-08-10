package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.interview.domain.DailyInterview;
import java.util.Optional;

public record TodayInterviewResult(String status, Long interviewId) {

    private static final String AVAILABLE = "AVAILABLE";

    public static TodayInterviewResult from(Optional<DailyInterview> interview) {
        return interview
                .map(it -> new TodayInterviewResult(it.getStatus().name(), it.getId()))
                .orElseGet(() -> new TodayInterviewResult(AVAILABLE, null));
    }
}
