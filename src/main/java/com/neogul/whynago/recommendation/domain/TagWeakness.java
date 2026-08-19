package com.neogul.whynago.recommendation.domain;

import com.neogul.whynago.question.domain.Category;

// 태그 1개의 약점도다. sampleCount는 그 태그로 실제 푼 문항 수이며,
// MIN_TRUSTED_SAMPLE 미만이면 weaknessScore가 소속 카테고리 값으로 폴백된 상태다.
public record TagWeakness(
        String name,
        Category category,
        double weaknessScore,
        int sampleCount
) {

    // 문제은행 규모가 작아 태그당 표본이 1건 수준이다. 우연한 오답 하나가 프로필을 지배하지 않게
    // 2건 이상만 태그 자체 값으로 신뢰한다.
    public static final int MIN_TRUSTED_SAMPLE = 2;

    public boolean trusted() {
        return sampleCount >= MIN_TRUSTED_SAMPLE;
    }
}
