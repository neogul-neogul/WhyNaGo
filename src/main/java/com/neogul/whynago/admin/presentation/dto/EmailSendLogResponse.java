package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.EmailSendLogResult;
import com.neogul.whynago.emailbatch.domain.EmailSendStatus;
import java.time.LocalDateTime;

public record EmailSendLogResponse(
        Long id,
        Long userId,
        String recipientEmail,
        LocalDateTime sentAt,
        EmailSendStatus status,
        String failureReason
) {

    public static EmailSendLogResponse from(EmailSendLogResult result) {
        return new EmailSendLogResponse(
                result.id(),
                result.userId(),
                result.recipientEmail(),
                result.sentAt(),
                result.status(),
                result.failureReason()
        );
    }
}
