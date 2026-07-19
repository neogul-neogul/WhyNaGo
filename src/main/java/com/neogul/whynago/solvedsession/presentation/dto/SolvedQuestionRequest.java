package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.SolvedQuestionCommand;
import jakarta.validation.constraints.NotNull;

public record SolvedQuestionRequest(
        @NotNull Long questionId,
        @NotNull Long choiceId,
        Long relationQuestionId
) {

    public SolvedQuestionCommand toCommand() {
        return new SolvedQuestionCommand(questionId, choiceId, relationQuestionId);
    }
}
