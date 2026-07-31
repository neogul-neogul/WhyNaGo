package com.neogul.whynago.user.presentation.dto;

import jakarta.validation.constraints.Min;

public record UpdateDailyGoalRequest(
        @Min(1) int dailyGoal
) {
}