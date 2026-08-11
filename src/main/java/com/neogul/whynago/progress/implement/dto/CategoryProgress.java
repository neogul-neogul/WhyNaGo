package com.neogul.whynago.progress.implement.dto;

import com.neogul.whynago.question.domain.Category;

public record CategoryProgress(
        Category category,
        int totalCount,
        int solvedCount,
        int correctCount,
        int score
) {
}
