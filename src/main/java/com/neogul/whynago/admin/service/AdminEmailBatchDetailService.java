package com.neogul.whynago.admin.service;

import com.neogul.whynago.admin.service.dto.EmailBatchExecutionDetailResult;
import com.neogul.whynago.admin.service.dto.EmailSendLogResult;
import com.neogul.whynago.admin.service.dto.EmailSendLogSearchCommand;
import com.neogul.whynago.admin.service.dto.EmailSendLogsResult;
import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import com.neogul.whynago.emailbatch.implement.EmailBatchHistoryReader;
import com.neogul.whynago.emailbatch.implement.dto.EmailSendLogPage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminEmailBatchDetailService {

    private final EmailBatchHistoryReader emailBatchHistoryReader;

    @Transactional(readOnly = true)
    public EmailBatchExecutionDetailResult readExecution(Long executionId) {
        EmailBatchExecution execution = emailBatchHistoryReader.readExecution(executionId);
        return EmailBatchExecutionDetailResult.of(
                execution, emailBatchHistoryReader.readFailureReasons(executionId));
    }

    @Transactional(readOnly = true)
    public EmailSendLogsResult readSendLogs(EmailSendLogSearchCommand command) {
        // 존재하지 않는 배치는 빈 목록이 아니라 404로 답한다.
        emailBatchHistoryReader.readExecution(command.executionId());

        EmailSendLogPage sendLogPage = emailBatchHistoryReader.readSendLogPage(
                command.executionId(), command.status(), command.page(), command.size());
        List<EmailSendLogResult> sendLogs = sendLogPage.sendLogs().stream()
                .map(EmailSendLogResult::from)
                .toList();

        return new EmailSendLogsResult(sendLogs, command.page(), command.size(), sendLogPage.totalElements());
    }
}
