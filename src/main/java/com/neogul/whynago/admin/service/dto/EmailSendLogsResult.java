package com.neogul.whynago.admin.service.dto;

import java.util.List;

public record EmailSendLogsResult(
        List<EmailSendLogResult> sendLogs,
        int page,
        int size,
        long totalElements
) {
}
