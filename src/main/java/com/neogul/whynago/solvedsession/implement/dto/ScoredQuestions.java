package com.neogul.whynago.solvedsession.implement.dto;

import java.util.List;

public record ScoredQuestions(List<ScoredQuestion> items, int totalCount, int correctCount) {

    public static ScoredQuestions from(List<ScoredQuestion> items) {
        int correctCount = (int) items.stream()
                .filter(ScoredQuestion::correct)
                .count();
        return new ScoredQuestions(items, items.size(), correctCount);
    }

    public int wrongCount() {
        return totalCount - correctCount;
    }

    public boolean hasWrongAnswer() {
        return wrongCount() > 0;
    }
}
