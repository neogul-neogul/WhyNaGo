package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.EssaySolvedQuestionCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record EssaySolvedQuestionRequest(
        Long questionId,
        @NotBlank String questionText,
        @NotBlank String userAnswer,
        @NotBlank String feedback,
        @NotBlank String modelAnswer,
        boolean isCorrect,
        // 선택 항목. 상한 클램핑은 도메인이 하므로 여기서는 형식만 본다.
        @PositiveOrZero Integer elapsedSeconds
) {

    public EssaySolvedQuestionCommand toCommand() {
        return new EssaySolvedQuestionCommand(
                questionId, questionText, userAnswer, feedback, modelAnswer, isCorrect, elapsedSeconds);
    }
}
