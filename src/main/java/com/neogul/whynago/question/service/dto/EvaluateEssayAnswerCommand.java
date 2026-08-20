package com.neogul.whynago.question.service.dto;

// elapsedSeconds는 클라이언트가 보고하지 않으면 null이며 "시간 신호 없음"을 뜻한다(0초가 아니다).
public record EvaluateEssayAnswerCommand(
        String conversationId,
        String question,
        String answer,
        Integer elapsedSeconds
) {
}
