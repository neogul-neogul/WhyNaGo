package com.neogul.whynago.progress.service.dto;

import com.neogul.whynago.progress.domain.Tier;

public record TierRange(Tier tier, int minScore) {

    public static TierRange from(Tier tier) {
        return new TierRange(tier, tier.minScore());
    }
}
