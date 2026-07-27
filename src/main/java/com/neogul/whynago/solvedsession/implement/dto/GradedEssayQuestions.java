package com.neogul.whynago.solvedsession.implement.dto;

import java.util.List;

public record GradedEssayQuestions(List<EssaySolvedPayload> items, int totalCount, int correctCount) {

    public static GradedEssayQuestions from(List<EssaySolvedPayload> items) {
        int correctCount = (int) items.stream()
                .filter(EssaySolvedPayload::isCorrect)
                .count();
        return new GradedEssayQuestions(items, items.size(), correctCount);
    }

    public int wrongCount() {
        return totalCount - correctCount;
    }

    public boolean hasWrongAnswer() {
        return wrongCount() > 0;
    }
}
