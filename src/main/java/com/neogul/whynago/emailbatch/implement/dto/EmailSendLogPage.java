package com.neogul.whynago.emailbatch.implement.dto;

import com.neogul.whynago.emailbatch.domain.EmailSendLog;
import java.util.List;

public record EmailSendLogPage(
        List<EmailSendLog> sendLogs,
        long totalElements
) {
}
