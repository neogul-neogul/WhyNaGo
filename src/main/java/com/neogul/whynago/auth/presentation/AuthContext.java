package com.neogul.whynago.auth.presentation;

import com.neogul.whynago.auth.domain.JwtClaim;
import com.neogul.whynago.user.domain.Role;

public record AuthContext(Long id, Role role) {

    public static AuthContext from(JwtClaim claim) {
        return new AuthContext(claim.id(), claim.role());
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}