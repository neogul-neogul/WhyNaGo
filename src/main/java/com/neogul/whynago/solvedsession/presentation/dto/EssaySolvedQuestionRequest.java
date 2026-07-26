package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.EssaySolvedQuestionCommand;
import jakarta.validation.constraints.NotBlank;

public record EssaySolvedQuestionRequest(
        Long questionId,
        @NotBlank String questionText,
        @NotBlank String userAnswer,
        @NotBlank String feedback,
        @NotBlank String modelAnswer,
        boolean isCorrect
) {

    public EssaySolvedQuestionCommand toCommand() {
        return new EssaySolvedQuestionCommand(questionId, questionText, userAnswer, feedback, modelAnswer, isCorrect);
    }
}
