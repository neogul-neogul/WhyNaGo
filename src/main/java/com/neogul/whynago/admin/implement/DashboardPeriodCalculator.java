package com.neogul.whynago.admin.implement;

import com.neogul.whynago.admin.implement.dto.DashboardPeriods;
import com.neogul.whynago.admin.implement.dto.DateTimeRange;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class DashboardPeriodCalculator {

    private static final int WEEK_DAYS = 7;

    // "최근 7일"은 오늘을 포함한 7일, "전주"는 그 직전 7일이다.
    public DashboardPeriods calculate(LocalDate today) {
        LocalDate yesterday = today.minusDays(1);

        return new DashboardPeriods(
                today,
                yesterday,
                dayRange(today),
                dayRange(yesterday),
                range(today.minusDays(WEEK_DAYS - 1), today),
                range(today.minusDays(WEEK_DAYS * 2 - 1), today.minusDays(WEEK_DAYS))
        );
    }

    private DateTimeRange dayRange(LocalDate date) {
        return range(date, date);
    }

    private DateTimeRange range(LocalDate from, LocalDate to) {
        return new DateTimeRange(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }
}
