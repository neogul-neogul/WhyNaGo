package com.neogul.whynago.solvedsession.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EssayScorePolicyTest {

    @DisplayName("서술형 점수를 0~10 범위로 자른다.")
    @ParameterizedTest(name = "{0}점 -> {1}점")
    @CsvSource({
            "-1, 0",
            "0, 0",
            "7, 7",
            "10, 10",
            "11, 10"
    })
    void normalize(int rawScore, int expected) {
        Integer normalized = EssayScorePolicy.normalize(rawScore);

        assertThat(normalized).isEqualTo(expected);
    }

    @DisplayName("점수를 보내지 않으면 0점이 아니라 점수 없음으로 남긴다.")
    @Test
    void normalizeWithNull() {
        Integer normalized = EssayScorePolicy.normalize(null);

        assertThat(normalized).isNull();
    }
}
