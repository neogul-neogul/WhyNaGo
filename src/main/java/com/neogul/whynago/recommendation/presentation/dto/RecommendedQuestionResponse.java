package com.neogul.whynago.recommendation.presentation.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.recommendation.service.dto.RecommendedQuestionResult;
import java.util.List;

public record RecommendedQuestionResponse(
        Long id,
        String title,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Category category,
        List<String> tags,
        boolean generated
) {

    public static RecommendedQuestionResponse from(RecommendedQuestionResult result) {
        return new RecommendedQuestionResponse(
                result.id(),
                result.title(),
                result.content(),
                result.type(),
                result.difficulty(),
                result.category(),
                result.tags(),
                result.generated()
        );
    }
}
