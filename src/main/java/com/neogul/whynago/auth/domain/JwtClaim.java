package com.neogul.whynago.auth.domain;

import com.neogul.whynago.user.domain.Role;

public record JwtClaim(Long id, Role role) {

    public static final String ID = "userId";
    public static final String ROLE = "role";
}