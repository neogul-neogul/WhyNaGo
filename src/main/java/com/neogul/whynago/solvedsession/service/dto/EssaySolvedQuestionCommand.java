package com.neogul.whynago.solvedsession.service.dto;

import com.neogul.whynago.solvedsession.implement.dto.EssaySolvedPayload;

public record EssaySolvedQuestionCommand(
        Long questionId,
        String questionText,
        String userAnswer,
        String feedback,
        String modelAnswer,
        boolean isCorrect
) {

    public EssaySolvedPayload toPayload() {
        return new EssaySolvedPayload(questionId, questionText, userAnswer, feedback, modelAnswer, isCorrect);
    }
}
