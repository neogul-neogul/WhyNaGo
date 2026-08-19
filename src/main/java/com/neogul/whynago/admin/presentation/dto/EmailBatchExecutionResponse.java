package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.EmailBatchExecutionResult;
import com.neogul.whynago.emailbatch.domain.EmailBatchStatus;
import java.time.LocalDateTime;

public record EmailBatchExecutionResponse(
        Long id,
        LocalDateTime executedAt,
        int totalTargetCount,
        int successCount,
        int failureCount,
        EmailBatchStatus status
) {

    public static EmailBatchExecutionResponse from(EmailBatchExecutionResult result) {
        return new EmailBatchExecutionResponse(
                result.id(),
                result.executedAt(),
                result.totalTargetCount(),
                result.successCount(),
                result.failureCount(),
                result.status()
        );
    }
}
