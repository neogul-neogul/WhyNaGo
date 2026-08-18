package com.neogul.whynago.solvedsession.implement.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GradedEssayQuestionsTest {

    @Test
    @DisplayName("문항의 정답 여부로 전체 수와 정답 수를 집계한다.")
    void from() {
        List<EssaySolvedPayload> items = List.of(
                payload(true),
                payload(false),
                payload(true)
        );

        GradedEssayQuestions graded = GradedEssayQuestions.from(items);

        assertThat(graded.totalCount()).isEqualTo(3);
        assertThat(graded.correctCount()).isEqualTo(2);
        assertThat(graded.wrongCount()).isEqualTo(1);
        assertThat(graded.hasWrongAnswer()).isTrue();
    }

    @Test
    @DisplayName("모두 정답이면 오답이 없다.")
    void hasNoWrongAnswer() {
        List<EssaySolvedPayload> items = List.of(payload(true), payload(true));

        GradedEssayQuestions graded = GradedEssayQuestions.from(items);

        assertThat(graded.correctCount()).isEqualTo(2);
        assertThat(graded.wrongCount()).isZero();
        assertThat(graded.hasWrongAnswer()).isFalse();
    }

    private EssaySolvedPayload payload(boolean isCorrect) {
        return new EssaySolvedPayload(null, "질문", "답변", "피드백", "모범답안", isCorrect, null, null);
    }
}
