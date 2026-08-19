package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import com.neogul.whynago.emailbatch.domain.EmailBatchStatus;
import java.time.LocalDateTime;

public record EmailBatchExecutionResult(
        Long id,
        LocalDateTime executedAt,
        int totalTargetCount,
        int successCount,
        int failureCount,
        EmailBatchStatus status
) {

    public static EmailBatchExecutionResult from(EmailBatchExecution execution) {
        return new EmailBatchExecutionResult(
                execution.getId(),
                execution.getExecutedAt(),
                execution.getTotalTargetCount(),
                execution.getSuccessCount(),
                execution.getFailureCount(),
                execution.getStatus()
        );
    }
}
