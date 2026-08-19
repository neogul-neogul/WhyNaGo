package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.FailureReasonResult;

public record FailureReasonResponse(
        String reason,
        long count
) {

    public static FailureReasonResponse from(FailureReasonResult result) {
        return new FailureReasonResponse(result.reason(), result.count());
    }
}
