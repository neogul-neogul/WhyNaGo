package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.AdminMemberResult;
import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.Position;
import java.time.LocalDateTime;

public record AdminMemberResponse(
        Long id,
        String nickname,
        String email,
        Position position,
        AuthProvider provider,
        LocalDateTime createdAt
) {

    public static AdminMemberResponse from(AdminMemberResult result) {
        return new AdminMemberResponse(
                result.id(),
                result.nickname(),
                result.email(),
                result.position(),
                result.provider(),
                result.createdAt()
        );
    }
}
