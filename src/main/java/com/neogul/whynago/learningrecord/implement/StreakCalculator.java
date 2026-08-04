package com.neogul.whynago.learningrecord.implement;

import com.neogul.whynago.learningrecord.implement.dto.StreakSummary;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StreakCalculator {

    // 오늘 아직 풀지 않았어도 어제까지 이어진 연속 학습일은 자정이 지나기 전까지 끊긴 것으로 보지 않는다.
    public StreakSummary calculate(List<LocalDate> solvedDates, LocalDate today) {
        Set<LocalDate> distinctDates = Set.copyOf(solvedDates);

        LocalDate cursor = distinctDates.contains(today) ? today : today.minusDays(1);
        int streakDays = 0;
        while (distinctDates.contains(cursor)) {
            streakDays++;
            cursor = cursor.minusDays(1);
        }

        return new StreakSummary(streakDays, distinctDates.size());
    }
}
