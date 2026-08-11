package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.AnswerInterviewCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerInterviewRequest(
        @NotBlank String question,
        @NotNull String answer
) {

    public AnswerInterviewCommand toCommand() {
        return new AnswerInterviewCommand(question, answer);
    }
}
