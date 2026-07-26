package com.neogul.whynago.solvedsession.implement.dto;

public record EssaySolvedPayload(
        Long questionId,
        String questionText,
        String userAnswer,
        String feedback,
        String modelAnswer,
        boolean isCorrect
) {
}
