package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.emailbatch.infra.dto.FailureReasonCount;

public record FailureReasonResult(
        String reason,
        long count
) {

    public static FailureReasonResult from(FailureReasonCount failureReasonCount) {
        return new FailureReasonResult(failureReasonCount.getReason(), failureReasonCount.getSendCount());
    }
}
