package com.neogul.whynago.common.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 서비스 기준 시간대는 KST 하나다. Clock.systemDefaultZone()을 쓰면 컨테이너 타임존 설정에
    // 의존하게 되므로, 존을 코드에 고정해 배포 환경과 무관하게 같은 날짜가 나오도록 한다.
    @Bean
    public Clock clock() {
        return Clock.system(KST);
    }
}
