package com.neogul.whynago.problemset.service.dto;

import com.neogul.whynago.problemset.domain.ProblemSet;

public record ProblemSetMembershipResult(
        Long id,
        String name,
        int itemCount,
        boolean saved
) {

    public static ProblemSetMembershipResult of(ProblemSet problemSet, int itemCount, boolean saved) {
        return new ProblemSetMembershipResult(
                problemSet.getId(),
                problemSet.getName(),
                itemCount,
                saved
        );
    }
}
