package com.neogul.whynago.question.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnswerChoiceTest {

    @Test
    @DisplayName("정답 여부와 다음 문제 식별자를 반환한다.")
    void correctAndNextQuestionId() {
        AnswerChoice answerChoice = AnswerChoice.create(1L, "정답", 1, true, "", 2L);

        assertThat(answerChoice.correct()).isTrue();
        assertThat(answerChoice.nextQuestionId()).isEqualTo(2L);
    }
}
