package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewAnswerSnapshotCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InterviewAnswerSnapshotRequest(
        @NotBlank String questionText,
        @NotNull String userAnswer,
        @NotBlank String feedback,
        @NotBlank String modelAnswer,
        boolean isCorrect,
        // 채점 API 응답의 grading.score를 그대로 담는다. 선택 항목이며 미전송이면 점수 신호 없음이다.
        @Min(0) @Max(10) Integer score,
        // 선택 항목. 상한 클램핑은 도메인이 하므로 여기서는 형식만 본다.
        @PositiveOrZero Integer elapsedSeconds
) {

    public InterviewAnswerSnapshotCommand toCommand() {
        return new InterviewAnswerSnapshotCommand(
                questionText, userAnswer, feedback, modelAnswer, isCorrect, score, elapsedSeconds);
    }
}
