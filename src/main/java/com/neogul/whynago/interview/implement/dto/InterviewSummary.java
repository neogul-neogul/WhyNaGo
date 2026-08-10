package com.neogul.whynago.interview.implement.dto;

import com.neogul.whynago.question.domain.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record InterviewSummary(
        Long interviewId,
        LocalDate interviewDate,
        Category category,
        String title,
        int totalCount,
        int correctCount,
        LocalDateTime completedAt
) {
}
