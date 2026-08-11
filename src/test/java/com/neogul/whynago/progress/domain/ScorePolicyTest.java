package com.neogul.whynago.progress.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScorePolicyTest {

    @Test
    @DisplayName("객관식은 난이도별로 하 3점, 중 4점, 상 5점이다.")
    void score_multipleChoice() {
        assertThat(ScorePolicy.score(QuestionType.MULTIPLE_CHOICE, Difficulty.LOW)).isEqualTo(3);
        assertThat(ScorePolicy.score(QuestionType.MULTIPLE_CHOICE, Difficulty.MEDIUM)).isEqualTo(4);
        assertThat(ScorePolicy.score(QuestionType.MULTIPLE_CHOICE, Difficulty.HIGH)).isEqualTo(5);
    }

    @Test
    @DisplayName("서술형은 같은 난이도의 객관식 점수보다 3배 높다.")
    void score_essay() {
        assertThat(ScorePolicy.score(QuestionType.ESSAY, Difficulty.LOW)).isEqualTo(9);
        assertThat(ScorePolicy.score(QuestionType.ESSAY, Difficulty.MEDIUM)).isEqualTo(12);
        assertThat(ScorePolicy.score(QuestionType.ESSAY, Difficulty.HIGH)).isEqualTo(15);
    }
}
