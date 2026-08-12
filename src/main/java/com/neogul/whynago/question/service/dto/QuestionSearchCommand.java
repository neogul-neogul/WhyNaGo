package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;

public record QuestionSearchCommand(
        QuestionType type,
        Difficulty difficulty,
        Category category,
        String keyword,
        int page,
        int size
) {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public static QuestionSearchCommand of(
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String keyword,
            Integer page,
            Integer size
    ) {
        return new QuestionSearchCommand(
                type,
                difficulty,
                category,
                keyword,
                normalizePage(page),
                normalizeSize(size)
        );
    }

    private static int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    private static int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
