package com.neogul.whynago.admin.presentation;

import com.neogul.whynago.admin.presentation.dto.EmailBatchExecutionDetailResponse;
import com.neogul.whynago.admin.presentation.dto.EmailBatchExecutionResponse;
import com.neogul.whynago.admin.presentation.dto.EmailSendLogResponse;
import com.neogul.whynago.admin.service.AdminEmailBatchDetailService;
import com.neogul.whynago.admin.service.AdminEmailBatchListService;
import com.neogul.whynago.admin.service.dto.EmailBatchExecutionsResult;
import com.neogul.whynago.admin.service.dto.EmailBatchSearchCommand;
import com.neogul.whynago.admin.service.dto.EmailSendLogSearchCommand;
import com.neogul.whynago.admin.service.dto.EmailSendLogsResult;
import com.neogul.whynago.common.presentation.dto.PageResponse;
import com.neogul.whynago.emailbatch.domain.EmailSendStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 조회는 요청자와 무관하고 권한은 AdminInterceptor가 이미 확인했으므로 로그인 사용자를 받지 않는다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/email-batches")
public class AdminEmailBatchController {

    private final AdminEmailBatchListService adminEmailBatchListService;
    private final AdminEmailBatchDetailService adminEmailBatchDetailService;

    @GetMapping
    public ResponseEntity<PageResponse<EmailBatchExecutionResponse>> findExecutions(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        EmailBatchSearchCommand command = EmailBatchSearchCommand.of(page, size);
        EmailBatchExecutionsResult result = adminEmailBatchListService.readExecutions(command);
        List<EmailBatchExecutionResponse> responses = result.executions().stream()
                .map(EmailBatchExecutionResponse::from)
                .toList();

        return ResponseEntity.ok(PageResponse.of(responses, result.page(), result.size(), result.totalElements()));
    }

    @GetMapping("/{executionId}")
    public ResponseEntity<EmailBatchExecutionDetailResponse> findExecution(@PathVariable Long executionId) {
        return ResponseEntity.ok(
                EmailBatchExecutionDetailResponse.from(adminEmailBatchDetailService.readExecution(executionId)));
    }

    @GetMapping("/{executionId}/send-logs")
    public ResponseEntity<PageResponse<EmailSendLogResponse>> findSendLogs(
            @PathVariable Long executionId,
            @RequestParam(required = false) EmailSendStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        EmailSendLogSearchCommand command = EmailSendLogSearchCommand.of(executionId, status, page, size);
        EmailSendLogsResult result = adminEmailBatchDetailService.readSendLogs(command);
        List<EmailSendLogResponse> responses = result.sendLogs().stream()
                .map(EmailSendLogResponse::from)
                .toList();

        return ResponseEntity.ok(PageResponse.of(responses, result.page(), result.size(), result.totalElements()));
    }
}
