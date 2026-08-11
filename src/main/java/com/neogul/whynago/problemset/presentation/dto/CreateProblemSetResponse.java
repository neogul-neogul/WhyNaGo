package com.neogul.whynago.problemset.presentation.dto;

import com.neogul.whynago.problemset.service.dto.CreateProblemSetResult;
import java.time.LocalDateTime;

public record CreateProblemSetResponse(
        Long id,
        String name,
        LocalDateTime updatedAt
) {

    public static CreateProblemSetResponse from(CreateProblemSetResult result) {
        return new CreateProblemSetResponse(
                result.id(),
                result.name(),
                result.updatedAt()
        );
    }
}
