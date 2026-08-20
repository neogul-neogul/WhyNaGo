package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.solvedsession.implement.dto.SolveCountByType;

public record CumulativeSolveCount(long total, long multipleChoiceCount, long essayCount) {

    public static CumulativeSolveCount from(SolveCountByType count) {
        return new CumulativeSolveCount(count.totalCount(), count.multipleChoiceCount(), count.essayCount());
    }
}
