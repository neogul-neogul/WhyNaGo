package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.AdminQuestionResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;

public record AdminQuestionResponse(
        Long id,
        String title,
        Category category,
        Difficulty difficulty,
        QuestionType type,
        long solveCount,
        Double correctRate
) {

    public static AdminQuestionResponse from(AdminQuestionResult result) {
        return new AdminQuestionResponse(
                result.id(),
                result.title(),
                result.category(),
                result.difficulty(),
                result.type(),
                result.solveCount(),
                result.correctRate()
        );
    }
}
