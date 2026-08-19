package com.neogul.whynago.emailbatch.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailBatchExecutionTest {

    private static final LocalDateTime EXECUTED_AT = LocalDateTime.of(2026, 8, 19, 21, 0, 0);

    @DisplayName("배치를 시작하면 대상 수만 확정되고 성공·실패 건수는 0이다.")
    @Test
    void start() {
        EmailBatchExecution execution = EmailBatchExecution.start(340, EXECUTED_AT);

        assertThat(execution.getExecutedAt()).isEqualTo(EXECUTED_AT);
        assertThat(execution.getTotalTargetCount()).isEqualTo(340);
        assertThat(execution.getSuccessCount()).isZero();
        assertThat(execution.getFailureCount()).isZero();
    }

    @DisplayName("실패가 없으면 상태가 SUCCESS다.")
    @Test
    void complete_success() {
        EmailBatchExecution execution = EmailBatchExecution.start(340, EXECUTED_AT);

        execution.complete(340, 0);

        assertThat(execution.getSuccessCount()).isEqualTo(340);
        assertThat(execution.getFailureCount()).isZero();
        assertThat(execution.getStatus()).isEqualTo(EmailBatchStatus.SUCCESS);
    }

    @DisplayName("성공과 실패가 섞여 있으면 상태가 PARTIAL_FAILURE다.")
    @Test
    void complete_partialFailure() {
        EmailBatchExecution execution = EmailBatchExecution.start(340, EXECUTED_AT);

        execution.complete(338, 2);

        assertThat(execution.getStatus()).isEqualTo(EmailBatchStatus.PARTIAL_FAILURE);
    }

    @DisplayName("대상이 있는데 성공이 하나도 없으면 상태가 FAILED다.")
    @Test
    void complete_failed() {
        EmailBatchExecution execution = EmailBatchExecution.start(3, EXECUTED_AT);

        execution.complete(0, 3);

        assertThat(execution.getStatus()).isEqualTo(EmailBatchStatus.FAILED);
    }

    @DisplayName("발송 대상이 없으면 실패가 아니므로 상태가 SUCCESS다.")
    @Test
    void complete_noTarget() {
        EmailBatchExecution execution = EmailBatchExecution.start(0, EXECUTED_AT);

        execution.complete(0, 0);

        assertThat(execution.getStatus()).isEqualTo(EmailBatchStatus.SUCCESS);
    }
}
