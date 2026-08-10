package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewAnswerSnapshotCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InterviewAnswerSnapshotRequest(
        @NotBlank String questionText,
        @NotNull String userAnswer,
        @NotBlank String feedback,
        @NotBlank String modelAnswer,
        boolean isCorrect
) {

    public InterviewAnswerSnapshotCommand toCommand() {
        return new InterviewAnswerSnapshotCommand(questionText, userAnswer, feedback, modelAnswer, isCorrect);
    }
}
