package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.MetricComparison;

public record MetricComparisonResponse(long current, long previous) {

    public static MetricComparisonResponse from(MetricComparison comparison) {
        return new MetricComparisonResponse(comparison.current(), comparison.previous());
    }
}
