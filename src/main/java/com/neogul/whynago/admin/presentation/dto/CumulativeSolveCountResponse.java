package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.CumulativeSolveCount;

public record CumulativeSolveCountResponse(long total, long multipleChoiceCount, long essayCount) {

    public static CumulativeSolveCountResponse from(CumulativeSolveCount count) {
        return new CumulativeSolveCountResponse(count.total(), count.multipleChoiceCount(), count.essayCount());
    }
}
