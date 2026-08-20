package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.AdminMemberSummaryResult;

public record AdminMemberSummaryResponse(
        long totalCount,
        long activeWeekCount
) {

    public static AdminMemberSummaryResponse from(AdminMemberSummaryResult result) {
        return new AdminMemberSummaryResponse(result.totalCount(), result.activeWeekCount());
    }
}
