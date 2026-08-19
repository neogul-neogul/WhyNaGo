package com.neogul.whynago.recommendation.presentation.dto;

import com.neogul.whynago.recommendation.service.dto.WeakTagResult;

public record WeakTagResponse(
        String tag,
        double weaknessScore,
        int sampleCount
) {

    public static WeakTagResponse from(WeakTagResult result) {
        return new WeakTagResponse(result.tag(), result.weaknessScore(), result.sampleCount());
    }
}
