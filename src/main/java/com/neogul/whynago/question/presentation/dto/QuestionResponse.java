package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.service.dto.QuestionResult;
import java.util.List;

public record QuestionResponse(
        Long id,
        String title,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Category category,
        String explanation,
        List<ChoiceResponse> choices,
        List<String> tags
) {

    public static QuestionResponse from(QuestionResult result) {
        return new QuestionResponse(
                result.id(),
                result.title(),
                result.content(),
                result.type(),
                result.difficulty(),
                result.category(),
                result.explanation(),
                result.choices().stream()
                        .map(ChoiceResponse::from)
                        .toList(),
                result.tags()
        );
    }
}
