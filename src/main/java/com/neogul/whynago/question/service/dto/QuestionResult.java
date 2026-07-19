package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import java.util.List;

public record QuestionResult(
        Long id,
        String title,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Category category,
        String explanation,
        List<ChoiceResult> choices,
        List<String> tags
) {

    public static QuestionResult from(Question question, List<ChoiceResult> choices, List<String> tags) {
        return new QuestionResult(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getType(),
                question.getDifficulty(),
                question.getCategory(),
                question.getExplanation(),
                choices,
                tags
        );
    }
}
