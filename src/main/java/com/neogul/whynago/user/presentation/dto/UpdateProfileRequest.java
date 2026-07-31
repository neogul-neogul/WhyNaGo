package com.neogul.whynago.user.presentation.dto;

import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.service.dto.UpdateProfileCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 4, max = 8) String nickname,
        @NotNull Position position,
        @Min(1) int dailyGoal,
        String bio
) {

    public UpdateProfileCommand toCommand() {
        return new UpdateProfileCommand(email, nickname, position, dailyGoal, bio);
    }
}
