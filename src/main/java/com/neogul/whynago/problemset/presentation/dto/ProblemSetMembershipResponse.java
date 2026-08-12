package com.neogul.whynago.problemset.presentation.dto;

import com.neogul.whynago.problemset.service.dto.ProblemSetMembershipResult;

public record ProblemSetMembershipResponse(
        Long id,
        String name,
        int itemCount,
        boolean saved
) {

    public static ProblemSetMembershipResponse from(ProblemSetMembershipResult result) {
        return new ProblemSetMembershipResponse(
                result.id(),
                result.name(),
                result.itemCount(),
                result.saved()
        );
    }
}
