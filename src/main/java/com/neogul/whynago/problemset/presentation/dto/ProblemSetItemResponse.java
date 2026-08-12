package com.neogul.whynago.problemset.presentation.dto;

import com.neogul.whynago.problemset.service.dto.ProblemSetItemResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;

public record ProblemSetItemResponse(
        Long questionId,
        String title,
        Category category,
        QuestionType type,
        Difficulty difficulty
) {

    static ProblemSetItemResponse from(ProblemSetItemResult result) {
        return new ProblemSetItemResponse(
                result.questionId(),
                result.title(),
                result.category(),
                result.type(),
                result.difficulty()
        );
    }
}
