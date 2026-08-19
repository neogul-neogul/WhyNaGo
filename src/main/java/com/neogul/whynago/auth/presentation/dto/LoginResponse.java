package com.neogul.whynago.auth.presentation.dto;

import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.domain.Role;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Long id,
        String email,
        String nickname,
        Position position,
        Role role
) {

    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(
                result.tokenPair().accessToken(),
                result.tokenPair().refreshToken(),
                result.userId(),
                result.email(),
                result.nickname(),
                result.position(),
                result.role());
    }
}
