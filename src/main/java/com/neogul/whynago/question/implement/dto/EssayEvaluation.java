package com.neogul.whynago.question.implement.dto;

// score는 AI가 항상 산출하므로 int다. 저장 경로에서 클라이언트가 중계하지 않았을 때만 null이 된다.
public record EssayEvaluation(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        String followupQuestion
) {
}
