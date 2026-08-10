package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.CompleteInterviewCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CompleteInterviewRequest(
        @NotNull @Valid InterviewAnswerSnapshotRequest rootQuestion,
        @NotNull
        @Size(min = 2, max = 2, message = "꼬리질문은 2개여야 합니다.")
        List<@Valid InterviewAnswerSnapshotRequest> followupQuestions,
        @PositiveOrZero int focusLossCount
) {

    public CompleteInterviewCommand toCommand() {
        return new CompleteInterviewCommand(
                rootQuestion.toCommand(),
                followupQuestions.stream()
                        .map(InterviewAnswerSnapshotRequest::toCommand)
                        .toList(),
                focusLossCount
        );
    }
}
