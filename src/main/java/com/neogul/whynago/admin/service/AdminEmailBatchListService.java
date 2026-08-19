package com.neogul.whynago.admin.service;

import com.neogul.whynago.admin.service.dto.EmailBatchExecutionResult;
import com.neogul.whynago.admin.service.dto.EmailBatchExecutionsResult;
import com.neogul.whynago.admin.service.dto.EmailBatchSearchCommand;
import com.neogul.whynago.emailbatch.implement.EmailBatchHistoryReader;
import com.neogul.whynago.emailbatch.implement.dto.EmailBatchExecutionPage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminEmailBatchListService {

    private final EmailBatchHistoryReader emailBatchHistoryReader;

    // 최근 실행이 먼저 보여야 하므로 실행 시각 역순 한 가지 순서만 제공한다.
    @Transactional(readOnly = true)
    public EmailBatchExecutionsResult readExecutions(EmailBatchSearchCommand command) {
        EmailBatchExecutionPage executionPage =
                emailBatchHistoryReader.readExecutionPage(command.page(), command.size());
        List<EmailBatchExecutionResult> executions = executionPage.executions().stream()
                .map(EmailBatchExecutionResult::from)
                .toList();

        return new EmailBatchExecutionsResult(
                executions, command.page(), command.size(), executionPage.totalElements());
    }
}
