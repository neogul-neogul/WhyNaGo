package com.neogul.whynago.emailbatch.fixture;

import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import com.neogul.whynago.emailbatch.domain.EmailSendLog;
import java.time.LocalDateTime;

public final class EmailBatchFixture {

    private static final LocalDateTime EXECUTED_AT = LocalDateTime.of(2026, 8, 19, 21, 0, 0);

    private EmailBatchFixture() {
    }

    public static EmailBatchExecution execution(int totalTargetCount) {
        return EmailBatchExecution.start(totalTargetCount, EXECUTED_AT);
    }

    public static EmailBatchExecution execution(int totalTargetCount, LocalDateTime executedAt) {
        return EmailBatchExecution.start(totalTargetCount, executedAt);
    }

    public static EmailBatchExecution completedExecution(int successCount, int failureCount) {
        return completedExecution(successCount, failureCount, EXECUTED_AT);
    }

    public static EmailBatchExecution completedExecution(
            int successCount,
            int failureCount,
            LocalDateTime executedAt
    ) {
        EmailBatchExecution execution = EmailBatchExecution.start(successCount + failureCount, executedAt);
        execution.complete(successCount, failureCount);
        return execution;
    }

    public static EmailSendLog successLog(Long batchExecutionId, Long userId, LocalDateTime sentAt) {
        return EmailSendLog.success(batchExecutionId, userId, "user" + userId + "@example.com", sentAt);
    }

    public static EmailSendLog failureLog(
            Long batchExecutionId,
            Long userId,
            LocalDateTime sentAt,
            String failureReason
    ) {
        return EmailSendLog.failure(
                batchExecutionId, userId, "user" + userId + "@example.com", sentAt, failureReason);
    }
}
