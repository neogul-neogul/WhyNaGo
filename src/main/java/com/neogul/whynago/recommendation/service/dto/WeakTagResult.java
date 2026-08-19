package com.neogul.whynago.recommendation.service.dto;

/** 전체 풀이 이력에서 계산한 태그별 약점도와 표본 수. */
public record WeakTagResult(
        String tag,
        double weaknessScore,
        int sampleCount
) {
}
