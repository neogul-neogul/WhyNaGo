package com.neogul.whynago.admin.implement;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.admin.implement.dto.DashboardPeriods;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DashboardPeriodCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 19);

    private final DashboardPeriodCalculator dashboardPeriodCalculator = new DashboardPeriodCalculator();

    @Test
    @DisplayName("오늘 구간은 자정부터 하루의 끝까지다.")
    void calculate_todayRange() {
        DashboardPeriods periods = dashboardPeriodCalculator.calculate(TODAY);

        assertThat(periods.today()).isEqualTo(TODAY);
        assertThat(periods.todayRange().from()).isEqualTo(LocalDateTime.of(2026, 8, 19, 0, 0));
        assertThat(periods.todayRange().to()).isEqualTo(TODAY.atTime(LocalTime.MAX));
    }

    @Test
    @DisplayName("전일 구간은 어제 하루다.")
    void calculate_yesterdayRange() {
        DashboardPeriods periods = dashboardPeriodCalculator.calculate(TODAY);

        assertThat(periods.yesterday()).isEqualTo(LocalDate.of(2026, 8, 18));
        assertThat(periods.yesterdayRange().from()).isEqualTo(LocalDateTime.of(2026, 8, 18, 0, 0));
        assertThat(periods.yesterdayRange().to()).isEqualTo(LocalDate.of(2026, 8, 18).atTime(LocalTime.MAX));
    }

    @Test
    @DisplayName("최근 7일은 오늘을 포함한 7일이다.")
    void calculate_recentWeekRange() {
        DashboardPeriods periods = dashboardPeriodCalculator.calculate(TODAY);

        assertThat(periods.recentWeekRange().from()).isEqualTo(LocalDateTime.of(2026, 8, 13, 0, 0));
        assertThat(periods.recentWeekRange().to()).isEqualTo(TODAY.atTime(LocalTime.MAX));
    }

    @Test
    @DisplayName("전주는 최근 7일 직전의 7일이라 구간이 겹치지 않는다.")
    void calculate_previousWeekRange() {
        DashboardPeriods periods = dashboardPeriodCalculator.calculate(TODAY);

        assertThat(periods.previousWeekRange().from()).isEqualTo(LocalDateTime.of(2026, 8, 6, 0, 0));
        assertThat(periods.previousWeekRange().to()).isEqualTo(LocalDate.of(2026, 8, 12).atTime(LocalTime.MAX));
        assertThat(periods.previousWeekRange().to()).isBefore(periods.recentWeekRange().from());
    }
}
