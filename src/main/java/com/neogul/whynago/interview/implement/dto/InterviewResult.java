package com.neogul.whynago.interview.implement.dto;

import com.neogul.whynago.interview.domain.InterviewStatus;
import com.neogul.whynago.question.domain.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InterviewResult(
        Long interviewId,
        LocalDate interviewDate,
        InterviewStatus status,
        Category category,
        int totalCount,
        int correctCount,
        int focusLossCount,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        long durationSeconds,
        List<InterviewResultItem> items
) {
}
