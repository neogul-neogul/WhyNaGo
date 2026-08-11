package com.neogul.whynago.progress.domain;

import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import java.util.Map;

public final class ScorePolicy {

    private static final Map<Difficulty, Integer> MULTIPLE_CHOICE_SCORE = Map.of(
            Difficulty.LOW, 1,
            Difficulty.MEDIUM, 2,
            Difficulty.HIGH, 3
    );
    private static final int ESSAY_MULTIPLIER = 4;

    private ScorePolicy() {
    }

    public static int score(QuestionType type, Difficulty difficulty) {
        int base = MULTIPLE_CHOICE_SCORE.get(difficulty);
        return type == QuestionType.ESSAY ? base * ESSAY_MULTIPLIER : base;
    }
}
