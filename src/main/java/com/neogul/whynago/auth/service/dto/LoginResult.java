package com.neogul.whynago.auth.service.dto;

import com.neogul.whynago.auth.domain.TokenPair;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.domain.Role;

public record LoginResult(
        TokenPair tokenPair,
        Long userId,
        String email,
        String nickname,
        Position position,
        Role role
) {
}