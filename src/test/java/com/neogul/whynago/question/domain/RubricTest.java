package com.neogul.whynago.question.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.RubricFixture;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RubricTest {

    @Test
    @DisplayName("충족한 항목의 배점을 합해 점수를 낸다.")
    void scoreOf_sumsOnlyMetCriteria() {
        Rubric rubric = RubricFixture.threeCriteria();

        assertThat(rubric.scoreOf(Set.of(1, 2))).isEqualTo(6);
    }

    @Test
    @DisplayName("모든 항목을 충족하면 배점 총합인 10점이 된다.")
    void scoreOf_allMetIsNotFullScore() {
        Rubric rubric = RubricFixture.threeCriteria();

        assertThat(rubric.scoreOf(Set.of(1, 2, 3))).isEqualTo(10);
    }

    @Test
    @DisplayName("충족한 항목이 없으면 0점이다.")
    void scoreOf_noneMet() {
        Rubric rubric = RubricFixture.threeCriteria();

        assertThat(rubric.scoreOf(Set.of())).isZero();
    }

    @Test
    @DisplayName("항목 범위를 벗어난 번호는 점수에 반영하지 않는다.")
    void scoreOf_countsOutOfRangeIndex() {
        Rubric rubric = RubricFixture.threeCriteria();

        assertThat(rubric.scoreOf(Set.of(1, 99))).isEqualTo(3);
    }

    @Test
    @DisplayName("배점 합이 10을 넘는 루브릭이라도 점수는 10을 넘지 않는다.")
    void scoreOf_exceedsMaxScore() {
        Rubric rubric = new Rubric(
                List.of(new RubricCriterion("항목1", 8), new RubricCriterion("항목2", 8)), null);

        assertThat(rubric.scoreOf(Set.of(1, 2))).isEqualTo(10);
    }

    @Test
    @DisplayName("항목이 없는 루브릭은 비어 있다고 판정한다.")
    void isEmpty_withoutCriteria() {
        assertThat(new Rubric(null, null).isEmpty()).isTrue();
        assertThat(new Rubric(List.of(), null).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("꼬리질문 범위가 비어 있으면 없는 것으로 본다.")
    void hasFollowupScope_emptyScope() {
        assertThat(RubricFixture.withoutFollowupScope().hasFollowupScope()).isFalse();
        assertThat(new Rubric(List.of(), new FollowupScope(List.of(), List.of())).hasFollowupScope()).isFalse();
        assertThat(RubricFixture.threeCriteria().hasFollowupScope()).isTrue();
    }
}
