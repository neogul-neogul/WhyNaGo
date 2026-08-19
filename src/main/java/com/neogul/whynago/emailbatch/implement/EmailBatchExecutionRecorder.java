package com.neogul.whynago.emailbatch.implement;

import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import com.neogul.whynago.emailbatch.domain.EmailSendLog;
import com.neogul.whynago.emailbatch.infra.EmailBatchExecutionRepository;
import com.neogul.whynago.emailbatch.infra.EmailSendLogRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 발송 성공·실패는 지나면 재계산할 수 없는 이벤트라, 배치가 도는 동안 발생 시점에 그대로 남긴다.
@Component
@RequiredArgsConstructor
public class EmailBatchExecutionRecorder {

    private final EmailBatchExecutionRepository emailBatchExecutionRepository;
    private final EmailSendLogRepository emailSendLogRepository;

    public EmailBatchExecution start(int totalTargetCount, LocalDateTime executedAt) {
        return emailBatchExecutionRepository.save(EmailBatchExecution.start(totalTargetCount, executedAt));
    }

    public void recordSuccess(
            EmailBatchExecution execution,
            Long userId,
            String recipientEmail,
            LocalDateTime sentAt
    ) {
        emailSendLogRepository.save(EmailSendLog.success(execution.getId(), userId, recipientEmail, sentAt));
    }

    public void recordFailure(
            EmailBatchExecution execution,
            Long userId,
            String recipientEmail,
            LocalDateTime sentAt,
            String failureReason
    ) {
        emailSendLogRepository.save(
                EmailSendLog.failure(execution.getId(), userId, recipientEmail, sentAt, failureReason));
    }

    public void complete(EmailBatchExecution execution, int successCount, int failureCount) {
        execution.complete(successCount, failureCount);
        emailBatchExecutionRepository.save(execution);
    }
}
