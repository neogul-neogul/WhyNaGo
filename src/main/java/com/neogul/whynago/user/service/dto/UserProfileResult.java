package com.neogul.whynago.user.service.dto;

import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.domain.User;

public record UserProfileResult(
        String nickname,
        String email,
        Position position,
        int dailyGoal
) {

    public static UserProfileResult from(User user) {
        return new UserProfileResult(
                user.getNickname(),
                user.getEmail().getValue(),
                user.getPosition(),
                user.getDailyGoal());
    }
}