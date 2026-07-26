package com.neogul.whynago.question.infra.ai;

public record GradeAndFollowupResult(String feedback, String modelAnswer, int score, String followupQuestion) {
}
