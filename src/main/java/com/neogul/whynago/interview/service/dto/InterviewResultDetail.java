package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.interview.implement.dto.InterviewResult;
import com.neogul.whynago.question.domain.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InterviewResultDetail(
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
        List<InterviewResultItemDetail> items
) {

    public static InterviewResultDetail from(InterviewResult result) {
        return new InterviewResultDetail(
                result.interviewId(),
                result.interviewDate(),
                result.status().name(),
                result.category(),
                result.totalCount(),
                result.correctCount(),
                result.focusLossCount(),
                result.startedAt(),
                result.completedAt(),
                result.durationSeconds(),
                result.items().stream()
                        .map(InterviewResultItemDetail::from)
                        .toList()
        );
    }
}
