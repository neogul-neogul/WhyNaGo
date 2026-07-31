package com.neogul.whynago.user.service.dto;

import com.neogul.whynago.user.domain.Position;

public record UpdateProfileCommand(
        String email,
        String nickname,
        Position position,
        int dailyGoal,
        String bio
) {
}
