package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.implement.dto.EssayQnA;
import jakarta.validation.constraints.NotBlank;

public record EssayTurnRequest(
        @NotBlank String question,
        @NotBlank String answer
) {

    public EssayQnA toEssayQnA() {
        return new EssayQnA(question, answer);
    }
}
