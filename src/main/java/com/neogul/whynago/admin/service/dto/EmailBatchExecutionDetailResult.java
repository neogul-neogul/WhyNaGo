package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import com.neogul.whynago.emailbatch.domain.EmailBatchStatus;
import com.neogul.whynago.emailbatch.infra.dto.FailureReasonCount;
import java.time.LocalDateTime;
import java.util.List;

public record EmailBatchExecutionDetailResult(
        Long id,
        LocalDateTime executedAt,
        int totalTargetCount,
        int successCount,
        int failureCount,
        EmailBatchStatus status,
        // 발송 상세 목록은 페이징되므로 실패 사유 요약은 전체를 집계해 함께 내려준다
        List<FailureReasonResult> failureReasons
) {

    public static EmailBatchExecutionDetailResult of(
            EmailBatchExecution execution,
            List<FailureReasonCount> failureReasons
    ) {
        return new EmailBatchExecutionDetailResult(
                execution.getId(),
                execution.getExecutedAt(),
                execution.getTotalTargetCount(),
                execution.getSuccessCount(),
                execution.getFailureCount(),
                execution.getStatus(),
                failureReasons.stream()
                        .map(FailureReasonResult::from)
                        .toList()
        );
    }
}
