package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.AnswerInterviewCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AnswerInterviewRequest(
        @NotBlank String question,
        @NotNull String answer,
        // 이번 문항에 답하는 데 걸린 시간(초). 측정하지 못했으면 보내지 않아도 된다.
        @PositiveOrZero Integer elapsedSeconds
) {

    public AnswerInterviewCommand toCommand() {
        return new AnswerInterviewCommand(question, answer, elapsedSeconds);
    }
}
