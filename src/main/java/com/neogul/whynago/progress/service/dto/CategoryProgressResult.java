package com.neogul.whynago.progress.service.dto;

import com.neogul.whynago.progress.implement.dto.CategoryProgress;
import com.neogul.whynago.question.domain.Category;

public record CategoryProgressResult(
        Category category,
        int totalCount,
        int solvedCount,
        int correctCount,
        int score
) {

    public static CategoryProgressResult from(CategoryProgress categoryProgress) {
        return new CategoryProgressResult(
                categoryProgress.category(),
                categoryProgress.totalCount(),
                categoryProgress.solvedCount(),
                categoryProgress.correctCount(),
                categoryProgress.score()
        );
    }
}
