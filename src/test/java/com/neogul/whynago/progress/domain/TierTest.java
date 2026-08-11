package com.neogul.whynago.progress.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TierTest {

    @Test
    @DisplayName("점수 구간에 맞는 티어를 반환한다.")
    void from() {
        assertThat(Tier.from(0)).isEqualTo(Tier.BRONZE);
        assertThat(Tier.from(57)).isEqualTo(Tier.BRONZE);
        assertThat(Tier.from(58)).isEqualTo(Tier.SILVER);
        assertThat(Tier.from(197)).isEqualTo(Tier.SILVER);
        assertThat(Tier.from(198)).isEqualTo(Tier.GOLD);
        assertThat(Tier.from(419)).isEqualTo(Tier.GOLD);
        assertThat(Tier.from(420)).isEqualTo(Tier.PLATINUM);
        assertThat(Tier.from(676)).isEqualTo(Tier.PLATINUM);
        assertThat(Tier.from(677)).isEqualTo(Tier.DIAMOND);
        assertThat(Tier.from(9999)).isEqualTo(Tier.DIAMOND);
    }

    @Test
    @DisplayName("다음 티어까지 필요한 점수를 계산한다.")
    void scoreToNext() {
        assertThat(Tier.BRONZE.scoreToNext(0)).isEqualTo(58);
        assertThat(Tier.SILVER.scoreToNext(100)).isEqualTo(98);
    }

    @Test
    @DisplayName("다이아몬드는 다음 티어가 없어 필요 점수가 0이다.")
    void scoreToNext_maxTier() {
        assertThat(Tier.DIAMOND.next()).isEmpty();
        assertThat(Tier.DIAMOND.scoreToNext(5000)).isZero();
    }
}
