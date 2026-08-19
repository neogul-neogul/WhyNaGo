package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.AdminQuestionDetailResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import java.util.List;

public record AdminQuestionDetailResponse(
        Long id,
        String title,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Category category,
        String explanation,
        List<AdminChoiceResponse> choices,
        List<String> tags,
        Long solveCount,
        Double correctRate
) {

    public static AdminQuestionDetailResponse from(AdminQuestionDetailResult result) {
        return new AdminQuestionDetailResponse(
                result.id(),
                result.title(),
                result.content(),
                result.type(),
                result.difficulty(),
                result.category(),
                result.explanation(),
                result.choices().stream()
                        .map(AdminChoiceResponse::from)
                        .toList(),
                result.tags(),
                result.solveCount(),
                result.correctRate()
        );
    }
}
