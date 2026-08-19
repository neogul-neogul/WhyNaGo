package com.neogul.whynago.recommendation.domain;

import com.neogul.whynago.common.domain.MasteryLevel;
import java.util.Map;

// 숙련도를 약점 가중치(0.0~1.0)로 옮긴다. 높을수록 그 주제를 더 모른다는 뜻이다.
// 가중치는 추천이 약점을 집계하는 방식이라 MasteryLevel 자체에 두지 않는다.
public final class MasteryWeight {

    private static final Map<MasteryLevel, Double> WEIGHTS = Map.of(
            MasteryLevel.MASTERED, 0.0,
            MasteryLevel.SOLID, 0.2,
            MasteryLevel.UNSTABLE, 0.5,
            // 찍어서 틀린 것은 오개념보다 태도 문제일 수 있어 WEAK보다 낮게 둔다.
            MasteryLevel.GUESSED, 0.7,
            MasteryLevel.WEAK, 0.85,
            MasteryLevel.NOT_LEARNED, 1.0
    );

    private MasteryWeight() {
    }

    public static double of(MasteryLevel level) {
        return WEIGHTS.get(level);
    }
}
