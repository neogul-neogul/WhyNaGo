package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.SolvedQuestionCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SolvedQuestionRequest(
        @NotNull Long questionId,
        @NotNull Long choiceId,
        Long relationQuestionId,
        // 아직 소요 시간을 보내지 않는 클라이언트도 저장할 수 있도록 선택 필드로 둔다.
        @PositiveOrZero Integer elapsedSeconds
) {

    public SolvedQuestionCommand toCommand() {
        return new SolvedQuestionCommand(questionId, choiceId, relationQuestionId, elapsedSeconds);
    }
}
