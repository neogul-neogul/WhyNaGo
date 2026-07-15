package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;

public record QuestionSearchCommand(
        QuestionType type,
        Difficulty difficulty,
        Category category,
        String keyword
) {
}
