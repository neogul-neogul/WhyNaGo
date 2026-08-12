package com.neogul.whynago.problemset.presentation.dto;

import com.neogul.whynago.problemset.service.dto.ProblemSetSummaryResult;
import java.time.LocalDateTime;
import java.util.List;

public record ProblemSetSummaryResponse(
        Long id,
        String name,
        int itemCount,
        List<String> previewTitles,
        LocalDateTime updatedAt
) {

    public static ProblemSetSummaryResponse from(ProblemSetSummaryResult result) {
        return new ProblemSetSummaryResponse(
                result.id(),
                result.name(),
                result.itemCount(),
                result.previewTitles(),
                result.updatedAt()
        );
    }
}
