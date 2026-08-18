package com.neogul.whynago.solvedsession.service.dto;

import com.neogul.whynago.solvedsession.implement.dto.SolvedQuestionPayload;

public record SolvedQuestionCommand(
        Long questionId,
        Long choiceId,
        Long relationQuestionId,
        Integer elapsedSeconds
) {

    public SolvedQuestionPayload toPayload() {
        return new SolvedQuestionPayload(questionId, choiceId, relationQuestionId, elapsedSeconds);
    }
}
