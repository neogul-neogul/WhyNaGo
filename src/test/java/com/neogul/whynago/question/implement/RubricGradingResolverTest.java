package com.neogul.whynago.question.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.fixture.RubricFixture;
import com.neogul.whynago.question.domain.Rubric;
import com.neogul.whynago.question.implement.dto.RubricEvaluation;
import com.neogul.whynago.question.implement.dto.RubricGrading;
import com.neogul.whynago.question.infra.ai.CriterionGrading;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RubricGradingResolverTest {

    private static final int AI_SCORE = 9;

    private final RubricGradingResolver rubricGradingResolver = new RubricGradingResolver();

    @Test
    @DisplayName("충족한 항목의 배점 합을 점수로 내고 항목별 판정을 함께 담는다.")
    void resolve_sumsMetWeights() {
        RubricGrading grading = rubricGradingResolver.resolve(
                RubricFixture.threeCriteria(),
                List.of(
                        new CriterionGrading(1, true, "짚었다."),
                        new CriterionGrading(2, false, "빠졌다."),
                        new CriterionGrading(3, true, "짚었다.")),
                AI_SCORE);

        assertThat(grading.score()).isEqualTo(7);
        assertThat(grading.criteria())
                .extracting(RubricEvaluation::weight, RubricEvaluation::met)
                .containsExactly(tuple(3, true), tuple(3, false), tuple(4, true));
    }

    @Test
    @DisplayName("응답 순서가 뒤섞여 와도 루브릭에 적힌 순서로 항목을 세운다.")
    void resolve_keepsResponseOrder() {
        RubricGrading grading = rubricGradingResolver.resolve(
                RubricFixture.threeCriteria(),
                List.of(
                        new CriterionGrading(3, true, "3번"),
                        new CriterionGrading(1, true, "1번"),
                        new CriterionGrading(2, true, "2번")),
                AI_SCORE);

        assertThat(grading.criteria())
                .extracting(RubricEvaluation::reason)
                .containsExactly("1번", "2번", "3번");
    }

    @Test
    @DisplayName("루브릭이 없으면 AI 점수를 그대로 쓰고 항목 판정은 비운다.")
    void resolve_withoutRubric() {
        assertThat(rubricGradingResolver.resolve(null, List.of(), AI_SCORE))
                .isEqualTo(new RubricGrading(AI_SCORE, List.of()));
        assertThat(rubricGradingResolver.resolve(new Rubric(List.of(), null), List.of(), AI_SCORE))
                .isEqualTo(new RubricGrading(AI_SCORE, List.of()));
    }

    @Test
    @DisplayName("판정 개수가 항목 수와 다르면 AI 점수로 폴백한다.")
    void resolve_countMismatch() {
        RubricGrading grading = rubricGradingResolver.resolve(
                RubricFixture.threeCriteria(),
                List.of(new CriterionGrading(1, true, "짚었다.")),
                AI_SCORE);

        assertThat(grading).isEqualTo(new RubricGrading(AI_SCORE, List.of()));
    }

    @Test
    @DisplayName("판정에 항목 번호가 없으면 AI 점수로 폴백한다.")
    void resolve_nullGradings() {
        RubricGrading grading = rubricGradingResolver.resolve(RubricFixture.threeCriteria(), null, AI_SCORE);

        assertThat(grading).isEqualTo(new RubricGrading(AI_SCORE, List.of()));
    }

    @Test
    @DisplayName("항목 번호가 범위를 벗어나면 AI 점수로 폴백한다.")
    void resolve_indexOutOfRange() {
        RubricGrading grading = rubricGradingResolver.resolve(
                RubricFixture.threeCriteria(),
                List.of(
                        new CriterionGrading(0, true, "짚었다."),
                        new CriterionGrading(2, true, "짚었다."),
                        new CriterionGrading(3, true, "짚었다.")),
                AI_SCORE);

        assertThat(grading).isEqualTo(new RubricGrading(AI_SCORE, List.of()));
    }

    @Test
    @DisplayName("항목 번호가 중복되면 AI 점수로 폴백한다.")
    void resolve_duplicatedIndex() {
        RubricGrading grading = rubricGradingResolver.resolve(
                RubricFixture.threeCriteria(),
                List.of(
                        new CriterionGrading(1, true, "짚었다."),
                        new CriterionGrading(1, true, "짚었다."),
                        new CriterionGrading(3, true, "짚었다.")),
                AI_SCORE);

        assertThat(grading).isEqualTo(new RubricGrading(AI_SCORE, List.of()));
    }
}
