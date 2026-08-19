package com.neogul.whynago.emailbatch.implement.dto;

import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import java.util.List;

public record EmailBatchExecutionPage(
        List<EmailBatchExecution> executions,
        long totalElements
) {
}
