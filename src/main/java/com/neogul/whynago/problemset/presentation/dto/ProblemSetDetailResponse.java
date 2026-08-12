package com.neogul.whynago.problemset.presentation.dto;

import com.neogul.whynago.problemset.service.dto.ProblemSetDetailResult;
import java.time.LocalDateTime;
import java.util.List;

public record ProblemSetDetailResponse(
        Long id,
        String name,
        LocalDateTime updatedAt,
        List<ProblemSetItemResponse> items
) {

    public static ProblemSetDetailResponse from(ProblemSetDetailResult result) {
        return new ProblemSetDetailResponse(
                result.id(),
                result.name(),
                result.updatedAt(),
                result.items().stream()
                        .map(ProblemSetItemResponse::from)
                        .toList()
        );
    }
}
