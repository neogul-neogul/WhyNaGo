package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.AdminMemberDetailResult;
import com.neogul.whynago.admin.service.dto.AdminMemberResult;
import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.Position;
import java.time.LocalDateTime;

public record AdminMemberDetailResponse(
        Long id,
        String nickname,
        String email,
        Position position,
        AuthProvider provider,
        LocalDateTime createdAt,
        int streakDays,
        long solvedQuestionCount,
        long completedInterviewCount
) {

    public static AdminMemberDetailResponse from(AdminMemberDetailResult result) {
        AdminMemberResult member = result.member();
        return new AdminMemberDetailResponse(
                member.id(),
                member.nickname(),
                member.email(),
                member.position(),
                member.provider(),
                member.createdAt(),
                result.streakDays(),
                result.solvedQuestionCount(),
                result.completedInterviewCount()
        );
    }
}
