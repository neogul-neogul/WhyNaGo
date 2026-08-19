package com.neogul.whynago.solvedsession.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ElapsedSecondsPolicyTest {

    @DisplayName("소요 시간이 상한을 넘으면 600초로 자른다.")
    @ParameterizedTest(name = "{0}초 -> {1}초")
    @CsvSource({
            "1, 1",
            "179, 179",
            "600, 600",
            "601, 600",
            "2147483647, 600"
    })
    void normalize(int rawSeconds, int expected) {
        Integer normalized = ElapsedSecondsPolicy.normalize(rawSeconds);

        assertThat(normalized).isEqualTo(expected);
    }

    @DisplayName("측정할 수 없는 소요 시간은 0이 아니라 null로 만든다.")
    @ParameterizedTest(name = "{0}초 -> null")
    @ValueSource(ints = {0, -1, -600})
    void normalizeWithNonPositive(int rawSeconds) {
        Integer normalized = ElapsedSecondsPolicy.normalize(rawSeconds);

        assertThat(normalized).isNull();
    }

    @DisplayName("소요 시간을 보내지 않으면 미측정으로 남긴다.")
    @Test
    void normalizeWithNull() {
        Integer normalized = ElapsedSecondsPolicy.normalize(null);

        assertThat(normalized).isNull();
    }
}
