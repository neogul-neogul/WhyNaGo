package com.neogul.whynago.admin.service.dto;

public record AdminMemberSummaryResult(
        long totalCount,
        long activeWeekCount
) {
}
