package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.solvedsession.implement.dto.EssaySolvedPayload;

public record InterviewAnswerSnapshotCommand(
        String questionText,
        String userAnswer,
        String feedback,
        String modelAnswer,
        boolean isCorrect
) {

    public EssaySolvedPayload toPayload() {
        return new EssaySolvedPayload(null, questionText, userAnswer, feedback, modelAnswer, isCorrect);
    }
}
