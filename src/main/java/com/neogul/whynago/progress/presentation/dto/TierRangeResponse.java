package com.neogul.whynago.progress.presentation.dto;

import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.service.dto.TierRange;

public record TierRangeResponse(Tier tier, int minScore) {

    public static TierRangeResponse from(TierRange tierRange) {
        return new TierRangeResponse(tierRange.tier(), tierRange.minScore());
    }
}
