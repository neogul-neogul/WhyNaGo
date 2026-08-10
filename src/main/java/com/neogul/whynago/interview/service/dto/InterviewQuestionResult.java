package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;

public record InterviewQuestionResult(
        Long id,
        String title,
        String content,
        Category category,
        Difficulty difficulty
) {

    public static InterviewQuestionResult from(Question question) {
        return new InterviewQuestionResult(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getCategory(),
                question.getDifficulty()
        );
    }
}
