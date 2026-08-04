package com.neogul.whynago.auth.presentation.dto;

import com.neogul.whynago.auth.service.dto.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String email,

        @NotBlank
        String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}