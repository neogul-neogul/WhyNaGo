package com.neogul.whynago.auth.presentation.dto;

import com.neogul.whynago.auth.service.dto.ReissueCommand;
import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(

        @NotBlank
        String refreshToken
) {

    public ReissueCommand toCommand() {
        return new ReissueCommand(refreshToken);
    }
}