package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.EmailBatchExecutionDetailResult;
import com.neogul.whynago.emailbatch.domain.EmailBatchStatus;
import java.time.LocalDateTime;
import java.util.List;

public record EmailBatchExecutionDetailResponse(
        Long id,
        LocalDateTime executedAt,
        int totalTargetCount,
        int successCount,
        int failureCount,
        EmailBatchStatus status,
        List<FailureReasonResponse> failureReasons
) {

    public static EmailBatchExecutionDetailResponse from(EmailBatchExecutionDetailResult result) {
        return new EmailBatchExecutionDetailResponse(
                result.id(),
                result.executedAt(),
                result.totalTargetCount(),
                result.successCount(),
                result.failureCount(),
                result.status(),
                result.failureReasons().stream()
                        .map(FailureReasonResponse::from)
                        .toList()
        );
    }
}
