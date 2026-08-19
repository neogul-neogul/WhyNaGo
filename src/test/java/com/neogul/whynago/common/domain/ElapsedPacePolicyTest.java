package com.neogul.whynago.common.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ElapsedPacePolicyTest {

    @ParameterizedTest(name = "{0}초 / 평균 200초 -> {1}")
    @CsvSource({
            // 0.7 미만이 FAST, 1.5 초과가 SLOW다. 경계값은 NORMAL에 붙는다.
            "100, FAST",
            "139, FAST",
            "140, NORMAL",
            "200, NORMAL",
            "300, NORMAL",
            "301, SLOW",
    })
    @DisplayName("평균 대비 비율로 빠름·보통·느림을 가른다.")
    void classify_wrongBoundary(int elapsedSeconds, ElapsedPace expected) {
        assertThat(ElapsedPacePolicy.classify(elapsedSeconds, 200, 10)).isEqualTo(expected);
    }

    @Test
    @DisplayName("소요 시간이 없으면 빠름·느림을 말할 수 없어 보통으로 본다.")
    void classify_unmeasured() {
        assertThat(ElapsedPacePolicy.classify(null, 200, 10)).isEqualTo(ElapsedPace.NORMAL);
    }

    @Test
    @DisplayName("표본이 5건 미만이면 평균을 믿지 않고 기준 시간을 쓴다.")
    void baseline_trustsSmallSample() {
        assertThat(ElapsedPacePolicy.baseline(30, 4)).isEqualTo(ElapsedPacePolicy.DEFAULT_ELAPSED_SECONDS);
        assertThat(ElapsedPacePolicy.baseline(30, 5)).isEqualTo(30);
    }

    @Test
    @DisplayName("평균이 없거나 0 이하면 기준 시간을 쓴다.")
    void baseline_withoutAverage() {
        assertThat(ElapsedPacePolicy.baseline(null, 100)).isEqualTo(ElapsedPacePolicy.DEFAULT_ELAPSED_SECONDS);
        assertThat(ElapsedPacePolicy.baseline(0, 100)).isEqualTo(ElapsedPacePolicy.DEFAULT_ELAPSED_SECONDS);
    }
}
