package com.neogul.whynago.progress.presentation.dto;

import com.neogul.whynago.progress.service.dto.CategoryProgressResult;
import com.neogul.whynago.question.domain.Category;

public record CategoryProgressResponse(
        Category category,
        int totalCount,
        int solvedCount,
        int correctCount,
        int score
) {

    public static CategoryProgressResponse from(CategoryProgressResult result) {
        return new CategoryProgressResponse(
                result.category(),
                result.totalCount(),
                result.solvedCount(),
                result.correctCount(),
                result.score()
        );
    }
}
