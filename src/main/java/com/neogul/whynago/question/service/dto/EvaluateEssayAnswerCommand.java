package com.neogul.whynago.question.service.dto;

public record EvaluateEssayAnswerCommand(String conversationId, String question, String answer) {
}
