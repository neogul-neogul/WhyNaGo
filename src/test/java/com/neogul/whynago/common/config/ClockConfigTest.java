package com.neogul.whynago.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.Clock;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ClockConfigTest extends IntegrationTestSupport {

    @Autowired
    private Clock clock;

    @DisplayName("Clock 빈은 Asia/Seoul 시간대를 사용한다.")
    @Test
    void clockZone() {
        // when
        ZoneId zone = clock.getZone();

        // then
        assertThat(zone).isEqualTo(ZoneId.of("Asia/Seoul"));
    }
}
