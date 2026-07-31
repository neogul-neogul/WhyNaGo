package com.neogul.whynago.user.presentation.dto;

import com.neogul.whynago.user.service.dto.UserProfileResult;

public record UserProfileResponse(int dailyGoal) {

    public static UserProfileResponse from(UserProfileResult result) {
        return new UserProfileResponse(result.dailyGoal());
    }
}