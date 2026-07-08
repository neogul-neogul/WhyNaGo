package com.neogul.whynago.auth.presentation.dto;

import com.neogul.whynago.user.domain.Position;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long id,
        String email,
        String nickname,
        Position position
) {
}