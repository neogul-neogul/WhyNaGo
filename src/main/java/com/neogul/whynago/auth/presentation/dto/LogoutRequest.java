package com.neogul.whynago.auth.presentation.dto;

import com.neogul.whynago.auth.service.dto.LogoutCommand;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(

        @NotBlank
        String refreshToken
) {

    public LogoutCommand toCommand() {
        return new LogoutCommand(refreshToken);
    }
}