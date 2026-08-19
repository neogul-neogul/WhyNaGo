package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;

public record AdminQuestionResult(
        Long id,
        String title,
        Category category,
        Difficulty difficulty,
        QuestionType type,
        long solveCount,
        // 풀이가 한 건도 없으면 null이다. 0%와 "아직 안 풀림"은 다르다.
        Double correctRate
) {

    public static AdminQuestionResult of(Question question, long solveCount, Double correctRate) {
        return new AdminQuestionResult(
                question.getId(),
                question.getTitle(),
                question.getCategory(),
                question.getDifficulty(),
                question.getType(),
                solveCount,
                correctRate
        );
    }
}
