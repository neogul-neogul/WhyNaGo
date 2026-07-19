package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.ChoiceGradingResult;

public record ChoiceGradingResponse(
        boolean correct,
        Long correctChoiceId,
        String explanation,
        String choiceExplanation,
        QuestionResponse nextQuestion
) {

    public static ChoiceGradingResponse from(ChoiceGradingResult result) {
        return new ChoiceGradingResponse(
                result.correct(),
                result.correctChoiceId(),
                result.explanation(),
                result.choiceExplanation(),
                result.nextQuestion() == null ? null : QuestionResponse.from(result.nextQuestion())
        );
    }
}
