package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewResultDetail;
import com.neogul.whynago.question.domain.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InterviewResultResponse(
        Long interviewId,
        LocalDate interviewDate,
        String status,
        Category category,
        int totalCount,
        int correctCount,
        int focusLossCount,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        long durationSeconds,
        List<InterviewResultItemResponse> items
) {

    public static InterviewResultResponse from(InterviewResultDetail detail) {
        return new InterviewResultResponse(
                detail.interviewId(),
                detail.interviewDate(),
                detail.status(),
                detail.category(),
                detail.totalCount(),
                detail.correctCount(),
                detail.focusLossCount(),
                detail.startedAt(),
                detail.completedAt(),
                detail.durationSeconds(),
                detail.items().stream()
                        .map(InterviewResultItemResponse::from)
                        .toList()
        );
    }
}
