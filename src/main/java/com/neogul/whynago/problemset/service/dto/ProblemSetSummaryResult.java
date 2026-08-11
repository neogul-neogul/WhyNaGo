package com.neogul.whynago.problemset.service.dto;

import com.neogul.whynago.problemset.domain.ProblemSet;
import java.time.LocalDateTime;
import java.util.List;

public record ProblemSetSummaryResult(
        Long id,
        String name,
        int itemCount,
        List<String> previewTitles,
        LocalDateTime updatedAt
) {

    public static ProblemSetSummaryResult of(ProblemSet problemSet, int itemCount, List<String> previewTitles) {
        return new ProblemSetSummaryResult(
                problemSet.getId(),
                problemSet.getName(),
                itemCount,
                previewTitles,
                problemSet.getUpdatedAt()
        );
    }
}
