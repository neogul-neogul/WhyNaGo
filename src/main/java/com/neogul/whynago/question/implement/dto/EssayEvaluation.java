package com.neogul.whynago.question.implement.dto;

public record EssayEvaluation(String feedback, String modelAnswer, boolean isCorrect, String followupQuestion) {
}
