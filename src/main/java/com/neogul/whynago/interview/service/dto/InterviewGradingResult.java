package com.neogul.whynago.interview.service.dto;

public record InterviewGradingResult(String feedback, String modelAnswer, int score, boolean isCorrect) {
}
