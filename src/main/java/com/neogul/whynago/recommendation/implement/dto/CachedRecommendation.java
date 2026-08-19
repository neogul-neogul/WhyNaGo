package com.neogul.whynago.recommendation.implement.dto;

import java.time.LocalDate;
import java.util.List;

// 사용자 1명의 추천 캐시 한 건. 프로필이 바뀌면 해시가 달라져 자동으로 무효가 된다.
public record CachedRecommendation(
        int profileHash,
        List<Long> questionIds,
        LocalDate cachedOn
) {

    public boolean isValid(int currentProfileHash, LocalDate today) {
        return profileHash == currentProfileHash && cachedOn.equals(today);
    }
}
