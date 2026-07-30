package com.neogul.whynago.learningrecord.service.dto;

import com.neogul.whynago.learningrecord.implement.dto.StreakSummary;

public record StreakResult(int streakDays, int cumulativeDays) {

    public static StreakResult from(StreakSummary streakSummary) {
        return new StreakResult(streakSummary.streakDays(), streakSummary.cumulativeDays());
    }
}