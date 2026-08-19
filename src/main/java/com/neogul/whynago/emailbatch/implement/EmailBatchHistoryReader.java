package com.neogul.whynago.emailbatch.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import com.neogul.whynago.emailbatch.domain.EmailSendLog;
import com.neogul.whynago.emailbatch.domain.EmailSendStatus;
import com.neogul.whynago.emailbatch.exception.EmailBatchErrorCode;
import com.neogul.whynago.emailbatch.implement.dto.EmailBatchExecutionPage;
import com.neogul.whynago.emailbatch.implement.dto.EmailSendLogPage;
import com.neogul.whynago.emailbatch.infra.EmailBatchExecutionRepository;
import com.neogul.whynago.emailbatch.infra.EmailSendLogRepository;
import com.neogul.whynago.emailbatch.infra.dto.FailureReasonCount;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailBatchHistoryReader {

    private final EmailBatchExecutionRepository emailBatchExecutionRepository;
    private final EmailSendLogRepository emailSendLogRepository;

    public EmailBatchExecutionPage readExecutionPage(int page, int size) {
        Page<EmailBatchExecution> executions =
                emailBatchExecutionRepository.findAllByOrderByExecutedAtDesc(PageRequest.of(page, size));
        return new EmailBatchExecutionPage(executions.getContent(), executions.getTotalElements());
    }

    public EmailBatchExecution readExecution(Long executionId) {
        return emailBatchExecutionRepository.findById(executionId)
                .orElseThrow(() -> new BusinessException(EmailBatchErrorCode.EMAIL_BATCH_NOT_FOUND));
    }

    // 상태 필터가 없으면 성공·실패를 모두 내려준다.
    public EmailSendLogPage readSendLogPage(Long executionId, EmailSendStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmailSendLog> sendLogs = status == null
                ? emailSendLogRepository.findAllByBatchExecutionIdOrderBySentAtAsc(executionId, pageable)
                : emailSendLogRepository.findAllByBatchExecutionIdAndStatusOrderBySentAtAsc(
                        executionId, status, pageable);
        return new EmailSendLogPage(sendLogs.getContent(), sendLogs.getTotalElements());
    }

    public List<FailureReasonCount> readFailureReasons(Long executionId) {
        return emailSendLogRepository.countGroupByFailureReason(executionId, EmailSendStatus.FAILURE);
    }
}
