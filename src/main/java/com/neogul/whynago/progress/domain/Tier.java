package com.neogul.whynago.progress.domain;

import java.util.Optional;

public enum Tier {

    BRONZE(0),
    SILVER(58),
    GOLD(198),
    PLATINUM(420),
    DIAMOND(677);

    private final int minScore;

    Tier(int minScore) {
        this.minScore = minScore;
    }

    public static Tier from(int score) {
        Tier result = BRONZE;
        for (Tier tier : values()) {
            if (score >= tier.minScore) {
                result = tier;
            }
        }
        return result;
    }

    public int minScore() {
        return minScore;
    }

    public Optional<Tier> next() {
        int nextOrdinal = ordinal() + 1;
        Tier[] tiers = values();
        return nextOrdinal < tiers.length ? Optional.of(tiers[nextOrdinal]) : Optional.empty();
    }

    // 다음 티어가 없으면(DIAMOND) 0을 반환한다.
    public int scoreToNext(int score) {
        return next().map(tier -> tier.minScore - score).orElse(0);
    }
}
