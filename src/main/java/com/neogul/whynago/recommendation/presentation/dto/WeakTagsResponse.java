package com.neogul.whynago.recommendation.presentation.dto;

import com.neogul.whynago.recommendation.service.dto.WeakTagsResult;
import java.util.List;

public record WeakTagsResponse(
        int sampleCount,
        List<WeakTagResponse> tags
) {

    public static WeakTagsResponse from(WeakTagsResult result) {
        return new WeakTagsResponse(
                result.sampleCount(),
                result.tags().stream().map(WeakTagResponse::from).toList()
        );
    }
}
