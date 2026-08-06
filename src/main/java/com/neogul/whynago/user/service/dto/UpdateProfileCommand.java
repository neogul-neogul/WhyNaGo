package com.neogul.whynago.user.service.dto;

import com.neogul.whynago.user.domain.Position;

public record UpdateProfileCommand(
        String nickname,
        Position position,
        int dailyGoal
) {
}
