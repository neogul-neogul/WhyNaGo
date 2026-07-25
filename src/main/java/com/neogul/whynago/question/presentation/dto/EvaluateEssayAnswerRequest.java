package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.EvaluateEssayAnswerCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EvaluateEssayAnswerRequest(
        @NotEmpty
        @Size(max = 3, message = "thread 항목 수는 최대 3개까지 허용됩니다.")
        @Valid
        List<EssayTurnRequest> thread
) {

    public EvaluateEssayAnswerCommand toCommand() {
        return new EvaluateEssayAnswerCommand(
                thread.stream()
                        .map(EssayTurnRequest::toEssayQnA)
                        .toList()
        );
    }
}
