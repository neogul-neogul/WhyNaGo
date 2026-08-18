package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.SolvedQuestionCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SolvedQuestionRequest(
        @NotNull Long questionId,
        @NotNull Long choiceId,
        Long relationQuestionId,
        // 선택 항목. 상한 클램핑은 도메인이 하므로 여기서는 형식만 본다.
        @PositiveOrZero Integer elapsedSeconds
) {

    public SolvedQuestionCommand toCommand() {
        return new SolvedQuestionCommand(questionId, choiceId, relationQuestionId, elapsedSeconds);
    }
}
