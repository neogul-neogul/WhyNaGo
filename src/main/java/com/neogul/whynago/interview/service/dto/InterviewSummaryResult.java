package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.interview.implement.dto.InterviewSummary;
import com.neogul.whynago.question.domain.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InterviewSummaryResult(
        Long interviewId,
        LocalDate interviewDate,
        Category category,
        String title,
        int totalCount,
        int correctCount,
        LocalDateTime completedAt
) {

    public static InterviewSummaryResult from(InterviewSummary summary) {
        return new InterviewSummaryResult(
                summary.interviewId(),
                summary.interviewDate(),
                summary.category(),
                summary.title(),
                summary.totalCount(),
                summary.correctCount(),
                summary.completedAt()
        );
    }
}
