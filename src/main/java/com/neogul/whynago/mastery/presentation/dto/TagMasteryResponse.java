package com.neogul.whynago.mastery.presentation.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.service.dto.TagMasteryResult;
import java.time.LocalDateTime;

public record TagMasteryResponse(
        Long tagId,
        String name,
        MasteryLevel level,
        String reason,
        LocalDateTime updatedAt
) {

    public static TagMasteryResponse from(TagMasteryResult result) {
        return new TagMasteryResponse(
                result.tagId(),
                result.name(),
                result.level(),
                result.reason(),
                result.updatedAt()
        );
    }
}
