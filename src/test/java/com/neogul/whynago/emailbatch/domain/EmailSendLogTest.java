package com.neogul.whynago.emailbatch.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailSendLogTest {

    private static final LocalDateTime SENT_AT = LocalDateTime.of(2026, 8, 19, 21, 0, 3);

    @DisplayName("성공 기록에는 실패 사유가 없다.")
    @Test
    void success() {
        EmailSendLog sendLog = EmailSendLog.success(1L, 501L, "user@example.com", SENT_AT);

        assertThat(sendLog.getStatus()).isEqualTo(EmailSendStatus.SUCCESS);
        assertThat(sendLog.getFailureReason()).isNull();
    }

    @DisplayName("실패 사유가 컬럼 길이를 넘으면 잘라서 저장한다.")
    @Test
    void failure_truncatesLongReason() {
        String longReason = "e".repeat(300);

        EmailSendLog sendLog = EmailSendLog.failure(1L, 501L, "user@example.com", SENT_AT, longReason);

        assertThat(sendLog.getStatus()).isEqualTo(EmailSendStatus.FAILURE);
        assertThat(sendLog.getFailureReason()).hasSize(255);
    }
}
