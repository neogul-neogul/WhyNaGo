package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewSummaryResult;
import com.neogul.whynago.question.domain.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InterviewSummaryResponse(
        Long interviewId,
        LocalDate interviewDate,
        Category category,
        String title,
        int totalCount,
        int correctCount,
        LocalDateTime completedAt
) {

    public static InterviewSummaryResponse from(InterviewSummaryResult result) {
        return new InterviewSummaryResponse(
                result.interviewId(),
                result.interviewDate(),
                result.category(),
                result.title(),
                result.totalCount(),
                result.correctCount(),
                result.completedAt()
        );
    }
}
