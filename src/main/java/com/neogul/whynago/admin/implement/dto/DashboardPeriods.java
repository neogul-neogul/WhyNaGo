package com.neogul.whynago.admin.implement.dto;

import java.time.LocalDate;

public record DashboardPeriods(
        LocalDate today,
        LocalDate yesterday,
        DateTimeRange todayRange,
        DateTimeRange yesterdayRange,
        DateTimeRange recentWeekRange,
        DateTimeRange previousWeekRange
) {
}
