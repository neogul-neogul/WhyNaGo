package com.neogul.whynago.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 12)
        String password,

        @NotBlank
        @Size(min = 4, max = 8)
        String nickname
) {
}