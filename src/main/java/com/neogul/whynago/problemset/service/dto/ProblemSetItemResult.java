package com.neogul.whynago.problemset.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;

public record ProblemSetItemResult(
        Long questionId,
        String title,
        Category category,
        QuestionType type,
        Difficulty difficulty
) {

    public static ProblemSetItemResult from(Question question) {
        return new ProblemSetItemResult(
                question.getId(),
                question.getTitle(),
                question.getCategory(),
                question.getType(),
                question.getDifficulty()
        );
    }
}
