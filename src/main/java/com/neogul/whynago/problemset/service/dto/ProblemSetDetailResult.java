package com.neogul.whynago.problemset.service.dto;

import com.neogul.whynago.problemset.domain.ProblemSet;
import java.time.LocalDateTime;
import java.util.List;

public record ProblemSetDetailResult(
        Long id,
        String name,
        LocalDateTime updatedAt,
        List<ProblemSetItemResult> items
) {

    public static ProblemSetDetailResult of(ProblemSet problemSet, List<ProblemSetItemResult> items) {
        return new ProblemSetDetailResult(
                problemSet.getId(),
                problemSet.getName(),
                problemSet.getUpdatedAt(),
                items
        );
    }
}
