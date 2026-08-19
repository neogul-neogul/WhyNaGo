package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.admin.implement.dto.DashboardAlert;
import java.util.List;

public record DashboardResult(
        long totalMemberCount,
        MetricComparison activeMember7Days,
        CumulativeSolveCount cumulativeSolveCount,
        MetricComparison todaySolveCount,
        MetricComparison todaySignUpCount,
        InterviewMetric todayInterview,
        List<DashboardAlert> alerts
) {
}
