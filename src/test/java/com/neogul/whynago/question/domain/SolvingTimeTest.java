package com.neogul.whynago.question.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.common.domain.ElapsedPacePolicy;
import com.neogul.whynago.common.domain.ElapsedSecondsPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SolvingTimeTest {

    @Test
    @DisplayName("평균보다 빠르면 점수를 1점 올린다.")
    void scoreAdjustment_ignoresFast() {
        SolvingTime solvingTime = SolvingTime.of(60, 200, 10);

        assertThat(solvingTime.pace()).isEqualTo(ElapsedPace.FAST);
        assertThat(solvingTime.scoreAdjustment()).isEqualTo(1);
    }

    @Test
    @DisplayName("평균보다 오래 걸리면 점수를 1점 내린다.")
    void scoreAdjustment_ignoresSlow() {
        SolvingTime solvingTime = SolvingTime.of(400, 200, 10);

        assertThat(solvingTime.pace()).isEqualTo(ElapsedPace.SLOW);
        assertThat(solvingTime.scoreAdjustment()).isEqualTo(-1);
    }

    @Test
    @DisplayName("평균 수준이면 점수를 조정하지 않는다.")
    void scoreAdjustment_adjustsNormal() {
        SolvingTime solvingTime = SolvingTime.of(200, 200, 10);

        assertThat(solvingTime.pace()).isEqualTo(ElapsedPace.NORMAL);
        assertThat(solvingTime.scoreAdjustment()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("0 이하로 보고된 시간은 측정 실패로 보고 채점에 반영하지 않는다.")
    void of_treatsNonPositiveAsMeasured(int rawSeconds) {
        SolvingTime solvingTime = SolvingTime.of(rawSeconds, 200, 10);

        assertThat(solvingTime.isMeasured()).isFalse();
        assertThat(solvingTime.scoreAdjustment()).isZero();
    }

    @Test
    @DisplayName("시간을 보고하지 않으면 측정하지 않은 것으로 두고 점수를 조정하지 않는다.")
    void unmeasured() {
        SolvingTime solvingTime = SolvingTime.unmeasured();

        assertThat(solvingTime.isMeasured()).isFalse();
        assertThat(solvingTime.elapsedSeconds()).isNull();
        assertThat(solvingTime.pace()).isEqualTo(ElapsedPace.NORMAL);
        assertThat(solvingTime.scoreAdjustment()).isZero();
    }

    @Test
    @DisplayName("보고된 시간이 상한을 넘으면 저장 경로와 같은 상한으로 자른다.")
    void of_exceedsMaxSeconds() {
        SolvingTime solvingTime = SolvingTime.of(ElapsedSecondsPolicy.MAX_SECONDS + 100, 200, 10);

        assertThat(solvingTime.elapsedSeconds()).isEqualTo(ElapsedSecondsPolicy.MAX_SECONDS);
    }

    @Test
    @DisplayName("문항 평균이 없으면 기준 시간을 기준선으로 삼는다.")
    void of_withoutAverage() {
        SolvingTime solvingTime = SolvingTime.of(180, null, 0);

        assertThat(solvingTime.baselineSeconds()).isEqualTo(ElapsedPacePolicy.DEFAULT_ELAPSED_SECONDS);
        assertThat(solvingTime.pace()).isEqualTo(ElapsedPace.NORMAL);
    }
}
