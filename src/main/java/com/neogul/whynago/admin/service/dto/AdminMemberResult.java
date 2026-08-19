package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.domain.User;
import java.time.LocalDateTime;

public record AdminMemberResult(
        Long id,
        String nickname,
        // 관리자에게는 마스킹하지 않은 원본을 내려준다(문의 대응·검색에 쓴다)
        String email,
        Position position,
        AuthProvider provider,
        // 가입 시각 추적 이전에 가입한 회원은 null이다
        LocalDateTime createdAt
) {

    public static AdminMemberResult from(User user) {
        return new AdminMemberResult(
                user.getId(),
                user.getNickname(),
                user.getEmail().getValue(),
                user.getPosition(),
                user.getProvider(),
                user.getCreatedAt()
        );
    }
}
