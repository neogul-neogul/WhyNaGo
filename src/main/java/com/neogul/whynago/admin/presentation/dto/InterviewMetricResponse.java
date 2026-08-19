package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.InterviewMetric;

public record InterviewMetricResponse(MetricComparisonResponse started, MetricComparisonResponse completed) {

    public static InterviewMetricResponse from(InterviewMetric metric) {
        return new InterviewMetricResponse(
                MetricComparisonResponse.from(metric.started()),
                MetricComparisonResponse.from(metric.completed())
        );
    }
}
