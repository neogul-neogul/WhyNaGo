package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.CreateEssaySolvedSessionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateEssaySolvedSessionRequest(
        @NotNull @Valid EssaySolvedQuestionRequest rootQuestion,
        @NotNull
        @Size(min = 2, max = 2, message = "꼬리질문은 2개여야 합니다.")
        List<@Valid EssaySolvedQuestionRequest> followupQuestions
) {

    public CreateEssaySolvedSessionCommand toCommand() {
        return new CreateEssaySolvedSessionCommand(
                rootQuestion.toCommand(),
                followupQuestions.stream()
                        .map(EssaySolvedQuestionRequest::toCommand)
                        .toList()
        );
    }
}
