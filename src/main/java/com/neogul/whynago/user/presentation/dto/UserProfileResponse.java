package com.neogul.whynago.user.presentation.dto;

import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.service.dto.UserProfileResult;

public record UserProfileResponse(
        String nickname,
        String email,
        Position position,
        int dailyGoal,
        String bio
) {

    public static UserProfileResponse from(UserProfileResult result) {
        return new UserProfileResponse(
                result.nickname(),
                result.email(),
                result.position(),
                result.dailyGoal(),
                result.bio());
    }
}