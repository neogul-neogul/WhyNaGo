package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewQuestionResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;

public record InterviewQuestionResponse(
        Long id,
        String title,
        String content,
        Category category,
        Difficulty difficulty
) {

    static InterviewQuestionResponse from(InterviewQuestionResult result) {
        return new InterviewQuestionResponse(
                result.id(),
                result.title(),
                result.content(),
                result.category(),
                result.difficulty()
        );
    }
}
