package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.CreateSolvedSessionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateSolvedSessionRequest(
        @NotNull @Valid SolvedQuestionRequest rootQuestion,
        @NotNull List<@Valid SolvedQuestionRequest> followupQuestions
) {

    public CreateSolvedSessionCommand toCommand() {
        return new CreateSolvedSessionCommand(
                rootQuestion.toCommand(),
                followupQuestions.stream()
                        .map(SolvedQuestionRequest::toCommand)
                        .toList()
        );
    }
}
