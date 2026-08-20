package com.neogul.whynago.admin.service.dto;

public record AdminMemberDetailResult(
        AdminMemberResult member,
        int streakDays,
        // 세션 수가 아니라 문항 수다(학습 기록의 일자별 문항 수와 같은 정의)
        long solvedQuestionCount,
        long completedInterviewCount
) {
}
