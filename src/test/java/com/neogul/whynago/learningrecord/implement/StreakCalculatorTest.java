package com.neogul.whynago.learningrecord.implement;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.learningrecord.implement.dto.StreakSummary;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StreakCalculatorTest {

    private final StreakCalculator streakCalculator = new StreakCalculator();

    @Test
    @DisplayName("오늘까지 연속으로 학습한 날짜 수만큼 스트릭을 센다.")
    void calculate_continuesThroughToday() {
        LocalDate today = LocalDate.of(2026, 6, 25);
        List<LocalDate> solvedDates = List.of(
                today,
                today.minusDays(1),
                today.minusDays(2),
                today.minusDays(10)
        );

        StreakSummary summary = streakCalculator.calculate(solvedDates, today);

        assertThat(summary.streakDays()).isEqualTo(3);
        assertThat(summary.cumulativeDays()).isEqualTo(4);
    }

    @Test
    @DisplayName("오늘 학습하지 않았어도 어제까지 이어졌다면 스트릭이 끊기지 않는다.")
    void calculate_gracePeriodBeforeToday() {
        LocalDate today = LocalDate.of(2026, 6, 25);
        List<LocalDate> solvedDates = List.of(today.minusDays(1), today.minusDays(2));

        StreakSummary summary = streakCalculator.calculate(solvedDates, today);

        assertThat(summary.streakDays()).isEqualTo(2);
    }

    @Test
    @DisplayName("어제도 학습하지 않았다면 스트릭은 0이다.")
    void calculate_broken() {
        LocalDate today = LocalDate.of(2026, 6, 25);
        List<LocalDate> solvedDates = List.of(today.minusDays(2));

        StreakSummary summary = streakCalculator.calculate(solvedDates, today);

        assertThat(summary.streakDays()).isZero();
        assertThat(summary.cumulativeDays()).isEqualTo(1);
    }

    @Test
    @DisplayName("학습 기록이 없으면 스트릭과 누적일 모두 0이다.")
    void calculate_empty() {
        StreakSummary summary = streakCalculator.calculate(List.of(), LocalDate.of(2026, 6, 25));

        assertThat(summary.streakDays()).isZero();
        assertThat(summary.cumulativeDays()).isZero();
    }
}