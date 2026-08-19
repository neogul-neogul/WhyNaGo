package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.emailbatch.domain.EmailSendLog;
import com.neogul.whynago.emailbatch.domain.EmailSendStatus;
import java.time.LocalDateTime;

public record EmailSendLogResult(
        Long id,
        Long userId,
        // 관리자에게는 마스킹하지 않은 발송 시점 주소를 내려준다(실패 원인 추적에 쓴다)
        String recipientEmail,
        LocalDateTime sentAt,
        EmailSendStatus status,
        String failureReason
) {

    public static EmailSendLogResult from(EmailSendLog sendLog) {
        return new EmailSendLogResult(
                sendLog.getId(),
                sendLog.getUserId(),
                sendLog.getRecipientEmail(),
                sendLog.getSentAt(),
                sendLog.getStatus(),
                sendLog.getFailureReason()
        );
    }
}
