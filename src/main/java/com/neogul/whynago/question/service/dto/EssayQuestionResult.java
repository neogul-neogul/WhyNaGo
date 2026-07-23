package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import java.util.List;

public record EssayQuestionResult(
        Long id,
        String title,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Category category,
        List<String> tags
) {

    public static EssayQuestionResult from(Question question, List<String> tags) {
        return new EssayQuestionResult(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getType(),
                question.getDifficulty(),
                question.getCategory(),
                tags
        );
    }
}
