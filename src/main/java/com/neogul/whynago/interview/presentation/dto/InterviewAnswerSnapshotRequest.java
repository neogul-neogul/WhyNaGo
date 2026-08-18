package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewAnswerSnapshotCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InterviewAnswerSnapshotRequest(
        @NotBlank String questionText,
        @NotNull String userAnswer,
        @NotBlank String feedback,
        @NotBlank String modelAnswer,
        boolean isCorrect,
        // 선택 항목. 상한 클램핑은 도메인이 하므로 여기서는 형식만 본다.
        @PositiveOrZero Integer elapsedSeconds
) {

    public InterviewAnswerSnapshotCommand toCommand() {
        return new InterviewAnswerSnapshotCommand(
                questionText, userAnswer, feedback, modelAnswer, isCorrect, elapsedSeconds);
    }
}
