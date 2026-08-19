package com.neogul.whynago.recommendation.service.dto;

import java.util.List;

/** 맞춤 추천 화면에 보여줄 전체 이력 기반 취약 태그 목록. */
public record WeakTagsResult(
        int sampleCount,
        List<WeakTagResult> tags
) {
}
