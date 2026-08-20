package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.EvaluateEssayAnswerCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record EvaluateEssayAnswerRequest(
        @NotBlank String conversationId,
        @NotBlank String question,
        @NotBlank String answer,
        // 이번 문항을 푸는 데 걸린 시간(초). 측정하지 못했으면 보내지 않아도 된다 - 그때는 시간을
        // 채점에 반영하지 않는다. 상한 클램핑은 도메인(ElapsedSecondsPolicy)이 하므로 여기서는 형식만 본다.
        @PositiveOrZero Integer elapsedSeconds
) {

    public EvaluateEssayAnswerCommand toCommand() {
        return new EvaluateEssayAnswerCommand(conversationId, question, answer, elapsedSeconds);
    }
}
