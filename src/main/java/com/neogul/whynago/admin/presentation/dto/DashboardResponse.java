package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.DashboardResult;
import java.util.List;

public record DashboardResponse(
        long totalMemberCount,
        MetricComparisonResponse activeMember7Days,
        CumulativeSolveCountResponse cumulativeSolveCount,
        MetricComparisonResponse todaySolveCount,
        MetricComparisonResponse todaySignUpCount,
        InterviewMetricResponse todayInterview,
        List<DashboardAlertResponse> alerts
) {

    public static DashboardResponse from(DashboardResult result) {
        return new DashboardResponse(
                result.totalMemberCount(),
                MetricComparisonResponse.from(result.activeMember7Days()),
                CumulativeSolveCountResponse.from(result.cumulativeSolveCount()),
                MetricComparisonResponse.from(result.todaySolveCount()),
                MetricComparisonResponse.from(result.todaySignUpCount()),
                InterviewMetricResponse.from(result.todayInterview()),
                result.alerts().stream()
                        .map(DashboardAlertResponse::from)
                        .toList()
        );
    }
}
