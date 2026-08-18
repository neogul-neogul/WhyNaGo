package com.neogul.whynago.admin.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.admin.implement.dto.ChoiceDistribution;
import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.solvedsession.implement.dto.QuestionSolveStatistics;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChoiceDistributionCalculatorTest {

    private static final Long QUESTION_ID = 1L;

    private final ChoiceDistributionCalculator choiceDistributionCalculator = new ChoiceDistributionCalculator();

    @Test
    @DisplayName("보기별 선택 수와 비율을 보기 순서대로 계산한다.")
    void calculate() {
        // given
        QuestionSolveStatistics statistics = new QuestionSolveStatistics(4, 3, Map.of(11L, 1L, 12L, 3L), null, 0);

        // when
        List<ChoiceDistribution> distributions = choiceDistributionCalculator.calculate(choices(), statistics);

        // then
        assertThat(distributions)
                .extracting(
                        ChoiceDistribution::sequence,
                        ChoiceDistribution::correct,
                        ChoiceDistribution::selectedCount,
                        ChoiceDistribution::selectedRate
                )
                .containsExactly(
                        tuple(1, false, 1L, 25.0),
                        tuple(2, true, 3L, 75.0),
                        tuple(3, false, 0L, 0.0),
                        tuple(4, false, 0L, 0.0)
                );
    }

    @Test
    @DisplayName("아무도 고르지 않은 보기도 0건으로 채워진다.")
    void calculate_unselectedChoice() {
        // given
        QuestionSolveStatistics statistics = new QuestionSolveStatistics(3, 3, Map.of(12L, 3L), null, 0);

        // when
        List<ChoiceDistribution> distributions = choiceDistributionCalculator.calculate(choices(), statistics);

        // then
        assertThat(distributions).hasSize(4);
        assertThat(distributions)
                .extracting(ChoiceDistribution::selectedCount)
                .containsExactly(0L, 3L, 0L, 0L);
    }

    @Test
    @DisplayName("보기가 교체되어 현재 목록에 없는 선택지의 응답은 분포에서 제외된다.")
    void calculate_removedChoice() {
        // given
        QuestionSolveStatistics statistics = new QuestionSolveStatistics(10, 4, Map.of(12L, 4L, 99L, 6L), null, 0);

        // when
        List<ChoiceDistribution> distributions = choiceDistributionCalculator.calculate(choices(), statistics);

        // then
        assertThat(distributions)
                .extracting(ChoiceDistribution::selectedCount)
                .containsExactly(0L, 4L, 0L, 0L);
        assertThat(distributions)
                .extracting(ChoiceDistribution::selectedRate)
                .containsExactly(0.0, 40.0, 0.0, 0.0);
    }

    @Test
    @DisplayName("가장 많이 고른 선택지를 반환한다.")
    void findMostChosen() {
        // given
        QuestionSolveStatistics statistics = new QuestionSolveStatistics(10, 3, Map.of(11L, 7L, 12L, 3L), null, 0);
        List<ChoiceDistribution> distributions = choiceDistributionCalculator.calculate(choices(), statistics);

        // when
        ChoiceDistribution mostChosen = choiceDistributionCalculator.findMostChosen(distributions);

        // then
        assertThat(mostChosen.sequence()).isEqualTo(1);
        assertThat(mostChosen.selectedCount()).isEqualTo(7);
        assertThat(mostChosen.correct()).isFalse();
    }

    @Test
    @DisplayName("가장 많이 고른 선택지가 동점이면 보기 순서가 빠른 것을 반환한다.")
    void findMostChosen_tie() {
        // given
        QuestionSolveStatistics statistics = new QuestionSolveStatistics(10, 5, Map.of(12L, 5L, 13L, 5L), null, 0);
        List<ChoiceDistribution> distributions = choiceDistributionCalculator.calculate(choices(), statistics);

        // when
        ChoiceDistribution mostChosen = choiceDistributionCalculator.findMostChosen(distributions);

        // then
        assertThat(mostChosen.sequence()).isEqualTo(2);
    }

    @Test
    @DisplayName("풀이가 없으면 가장 많이 고른 선택지가 없고 비율이 모두 0이다.")
    void findMostChosen_noRecord() {
        // given
        QuestionSolveStatistics statistics = new QuestionSolveStatistics(0, 0, Map.of(), null, 0);
        List<ChoiceDistribution> distributions = choiceDistributionCalculator.calculate(choices(), statistics);

        // when
        ChoiceDistribution mostChosen = choiceDistributionCalculator.findMostChosen(distributions);

        // then
        assertThat(mostChosen).isNull();
        assertThat(distributions)
                .extracting(ChoiceDistribution::selectedRate)
                .containsOnly(0.0);
    }

    @Test
    @DisplayName("비율은 소수점 첫째 자리로 반올림된다.")
    void rate() {
        // when & then
        assertThat(choiceDistributionCalculator.rate(1175, 1842)).isEqualTo(63.8);
        assertThat(choiceDistributionCalculator.rate(1, 3)).isEqualTo(33.3);
        assertThat(choiceDistributionCalculator.rate(2, 3)).isEqualTo(66.7);
    }

    @Test
    @DisplayName("풀이가 없으면 비율은 0이다.")
    void rate_zeroTotal() {
        // when & then
        assertThat(choiceDistributionCalculator.rate(0, 0)).isZero();
    }

    private List<AnswerChoice> choices() {
        return List.of(
                AnswerChoiceFixture.withId(11L, AnswerChoiceFixture.wrong(QUESTION_ID, 1)),
                AnswerChoiceFixture.withId(12L, AnswerChoiceFixture.correct(QUESTION_ID, 2, null)),
                AnswerChoiceFixture.withId(13L, AnswerChoiceFixture.wrong(QUESTION_ID, 3)),
                AnswerChoiceFixture.withId(14L, AnswerChoiceFixture.wrong(QUESTION_ID, 4))
        );
    }
}
