package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.StartInterviewResult;
import java.time.LocalDateTime;

public record StartInterviewResponse(
        Long interviewId,
        InterviewQuestionResponse question,
        int totalQuestionCount,
        int timeLimitSeconds,
        LocalDateTime startedAt
) {

    public static StartInterviewResponse from(StartInterviewResult result) {
        return new StartInterviewResponse(
                result.interviewId(),
                InterviewQuestionResponse.from(result.question()),
                result.totalQuestionCount(),
                result.timeLimitSeconds(),
                result.startedAt()
        );
    }
}
