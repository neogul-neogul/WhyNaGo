package com.neogul.whynago.mastery.service.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import java.time.LocalDateTime;

public record TagMasteryResult(
        Long tagId,
        String name,
        MasteryLevel level,
        // 그 판정을 받은 근거. AI 판정이면 답변에서 근거를 짚은 문장이다.
        String reason,
        LocalDateTime updatedAt
) {
}
