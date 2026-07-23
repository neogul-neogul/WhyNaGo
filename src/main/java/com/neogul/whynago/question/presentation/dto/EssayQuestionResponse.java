package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.service.dto.EssayQuestionResult;
import java.util.List;

public record EssayQuestionResponse(
        Long id,
        String title,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Category category,
        List<String> tags
) {

    public static EssayQuestionResponse from(EssayQuestionResult result) {
        return new EssayQuestionResponse(
                result.id(),
                result.title(),
                result.content(),
                result.type(),
                result.difficulty(),
                result.category(),
                result.tags()
        );
    }
}
