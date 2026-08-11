package com.neogul.whynago.progress.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScorePolicyTest {

    @Test
    @DisplayName("객관식은 난이도별로 하 1점, 중 2점, 상 3점이다.")
    void score_multipleChoice() {
        assertThat(ScorePolicy.score(QuestionType.MULTIPLE_CHOICE, Difficulty.LOW)).isEqualTo(1);
        assertThat(ScorePolicy.score(QuestionType.MULTIPLE_CHOICE, Difficulty.MEDIUM)).isEqualTo(2);
        assertThat(ScorePolicy.score(QuestionType.MULTIPLE_CHOICE, Difficulty.HIGH)).isEqualTo(3);
    }

    @Test
    @DisplayName("서술형은 같은 난이도의 객관식 점수보다 4배 높다.")
    void score_essay() {
        assertThat(ScorePolicy.score(QuestionType.ESSAY, Difficulty.LOW)).isEqualTo(4);
        assertThat(ScorePolicy.score(QuestionType.ESSAY, Difficulty.MEDIUM)).isEqualTo(8);
        assertThat(ScorePolicy.score(QuestionType.ESSAY, Difficulty.HIGH)).isEqualTo(12);
    }
}
