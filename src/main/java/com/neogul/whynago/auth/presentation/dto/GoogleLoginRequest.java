package com.neogul.whynago.auth.presentation.dto;

import com.neogul.whynago.auth.service.dto.GoogleLoginCommand;
import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(

        @NotBlank
        String credential
) {

    public GoogleLoginCommand toCommand() {
        return new GoogleLoginCommand(credential);
    }
}