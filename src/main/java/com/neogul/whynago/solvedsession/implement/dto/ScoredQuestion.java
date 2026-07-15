package com.neogul.whynago.solvedsession.implement.dto;

public record ScoredQuestion(
        Long questionId,
        Long userChoiceId,
        Long answerChoiceId,
        boolean correct
) {
}
