package com.neogul.whynago.solvedsession.implement.dto;

public record SolvedQuestionPayload(
        Long questionId,
        Long choiceId,
        Long relationQuestionId
) {
}
