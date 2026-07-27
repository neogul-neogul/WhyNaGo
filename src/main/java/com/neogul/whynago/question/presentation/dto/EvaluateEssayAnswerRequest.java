package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.EvaluateEssayAnswerCommand;
import jakarta.validation.constraints.NotBlank;

public record EvaluateEssayAnswerRequest(
        @NotBlank String conversationId,
        @NotBlank String question,
        @NotBlank String answer
) {

    public EvaluateEssayAnswerCommand toCommand() {
        return new EvaluateEssayAnswerCommand(conversationId, question, answer);
    }
}
