package com.neogul.whynago.learningrecord.presentation.dto;

import com.neogul.whynago.learningrecord.service.dto.StreakResult;

public record StreakResponse(int streakDays, int cumulativeDays) {

    public static StreakResponse from(StreakResult result) {
        return new StreakResponse(result.streakDays(), result.cumulativeDays());
    }
}