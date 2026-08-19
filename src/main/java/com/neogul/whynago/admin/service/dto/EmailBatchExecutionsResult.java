package com.neogul.whynago.admin.service.dto;

import java.util.List;

public record EmailBatchExecutionsResult(
        List<EmailBatchExecutionResult> executions,
        int page,
        int size,
        long totalElements
) {
}
