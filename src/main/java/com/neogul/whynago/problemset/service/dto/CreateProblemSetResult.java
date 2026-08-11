package com.neogul.whynago.problemset.service.dto;

import com.neogul.whynago.problemset.domain.ProblemSet;
import java.time.LocalDateTime;

public record CreateProblemSetResult(
        Long id,
        String name,
        LocalDateTime updatedAt
) {

    public static CreateProblemSetResult from(ProblemSet problemSet) {
        return new CreateProblemSetResult(
                problemSet.getId(),
                problemSet.getName(),
                problemSet.getUpdatedAt()
        );
    }
}
