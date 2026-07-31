package com.neogul.whynago.user.service.dto;

import com.neogul.whynago.user.domain.User;

public record UserProfileResult(int dailyGoal) {

    public static UserProfileResult from(User user) {
        return new UserProfileResult(user.getDailyGoal());
    }
}