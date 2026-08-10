package com.neogul.whynago.auth.infra.dto;

public record GoogleUserInfo(
        String sub,
        String email,
        boolean emailVerified
) {
}