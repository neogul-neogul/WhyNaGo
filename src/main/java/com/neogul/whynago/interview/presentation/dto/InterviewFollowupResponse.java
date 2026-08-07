package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewFollowupResult;

public record InterviewFollowupResponse(String question) {

    static InterviewFollowupResponse from(InterviewFollowupResult result) {
        return new InterviewFollowupResponse(result.question());
    }
}
