package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.question.domain.Question;
import java.time.LocalDateTime;

public record StartInterviewResult(
        Long interviewId,
        InterviewQuestionResult question,
        int totalQuestionCount,
        int timeLimitSeconds,
        LocalDateTime startedAt
) {

    public static StartInterviewResult of(
            DailyInterview interview,
            Question question,
            int totalQuestionCount,
            int timeLimitSeconds
    ) {
        return new StartInterviewResult(
                interview.getId(),
                InterviewQuestionResult.from(question),
                totalQuestionCount,
                timeLimitSeconds,
                interview.getStartedAt()
        );
    }
}
