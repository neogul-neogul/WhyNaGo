package com.neogul.whynago.fixture;

import com.neogul.whynago.question.domain.SolvingTime;

// 문항 통계가 없을 때의 기준 시간 180초를 전제로, 그 대비 비율이 임계값(0.7 / 1.5) 밖·안에 오게 잡은 값이다.
public final class SolvingTimeFixture {

    private SolvingTimeFixture() {
    }

    // 100 / 180 = 0.55 -> FAST
    public static SolvingTime fast() {
        return SolvingTime.of(100, null, 0);
    }

    // 180 / 180 = 1.0 -> NORMAL
    public static SolvingTime normal() {
        return SolvingTime.of(180, null, 0);
    }

    // 300 / 180 = 1.67 -> SLOW
    public static SolvingTime slow() {
        return SolvingTime.of(300, null, 0);
    }

    public static SolvingTime unmeasured() {
        return SolvingTime.unmeasured();
    }
}
