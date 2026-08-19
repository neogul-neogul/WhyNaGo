package com.neogul.whynago.recommendation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.common.domain.MasteryLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MasteryWeightTest {

    @Test
    @DisplayName("숙련도가 높을수록 약점 가중치가 낮다.")
    void weaknessWeightOrder() {
        assertThat(MasteryWeight.of(MasteryLevel.MASTERED)).isZero();
        assertThat(MasteryWeight.of(MasteryLevel.MASTERED))
                .isLessThan(MasteryWeight.of(MasteryLevel.SOLID));
        assertThat(MasteryWeight.of(MasteryLevel.SOLID))
                .isLessThan(MasteryWeight.of(MasteryLevel.UNSTABLE));
        assertThat(MasteryWeight.of(MasteryLevel.UNSTABLE))
                .isLessThan(MasteryWeight.of(MasteryLevel.GUESSED));
        assertThat(MasteryWeight.of(MasteryLevel.GUESSED))
                .isLessThan(MasteryWeight.of(MasteryLevel.WEAK));
        assertThat(MasteryWeight.of(MasteryLevel.WEAK))
                .isLessThan(MasteryWeight.of(MasteryLevel.NOT_LEARNED));
        assertThat(MasteryWeight.of(MasteryLevel.NOT_LEARNED)).isEqualTo(1.0);
    }
}
