package com.neogul.whynago.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.admin.service.dto.EmailBatchExecutionDetailResult;
import com.neogul.whynago.admin.service.dto.EmailSendLogResult;
import com.neogul.whynago.admin.service.dto.EmailSendLogSearchCommand;
import com.neogul.whynago.admin.service.dto.EmailSendLogsResult;
import com.neogul.whynago.admin.service.dto.FailureReasonResult;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import com.neogul.whynago.emailbatch.domain.EmailBatchStatus;
import com.neogul.whynago.emailbatch.domain.EmailSendStatus;
import com.neogul.whynago.emailbatch.exception.EmailBatchErrorCode;
import com.neogul.whynago.emailbatch.fixture.EmailBatchFixture;
import com.neogul.whynago.emailbatch.infra.EmailBatchExecutionRepository;
import com.neogul.whynago.emailbatch.infra.EmailSendLogRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminEmailBatchDetailServiceTest extends IntegrationTestSupport {

    private static final LocalDateTime SENT_AT = LocalDateTime.of(2026, 8, 19, 21, 0, 0);

    @Autowired
    private AdminEmailBatchDetailService adminEmailBatchDetailService;

    @Autowired
    private EmailBatchExecutionRepository emailBatchExecutionRepository;

    @Autowired
    private EmailSendLogRepository emailSendLogRepository;

    @DisplayName("배치 단건과 실패 사유 요약을 조회한다.")
    @Test
    void readExecution() {
        EmailBatchExecution execution =
                emailBatchExecutionRepository.save(EmailBatchFixture.completedExecution(1, 3));
        emailSendLogRepository.save(EmailBatchFixture.successLog(execution.getId(), 1L, SENT_AT));
        emailSendLogRepository.save(EmailBatchFixture.failureLog(execution.getId(), 2L, SENT_AT, "Mailbox full"));
        emailSendLogRepository.save(EmailBatchFixture.failureLog(execution.getId(), 3L, SENT_AT, "Invalid address"));
        emailSendLogRepository.save(EmailBatchFixture.failureLog(execution.getId(), 4L, SENT_AT, "Invalid address"));

        EmailBatchExecutionDetailResult result = adminEmailBatchDetailService.readExecution(execution.getId());

        assertThat(result.totalTargetCount()).isEqualTo(4);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(3);
        assertThat(result.status()).isEqualTo(EmailBatchStatus.PARTIAL_FAILURE);
        assertThat(result.failureReasons())
                .extracting(FailureReasonResult::reason, FailureReasonResult::count)
                .containsExactly(
                        tuple("Invalid address", 2L),
                        tuple("Mailbox full", 1L)
                );
    }

    @DisplayName("실패가 없는 배치의 실패 사유 요약은 비어 있다.")
    @Test
    void readExecution_noFailure() {
        EmailBatchExecution execution =
                emailBatchExecutionRepository.save(EmailBatchFixture.completedExecution(1, 0));
        emailSendLogRepository.save(EmailBatchFixture.successLog(execution.getId(), 1L, SENT_AT));

        EmailBatchExecutionDetailResult result = adminEmailBatchDetailService.readExecution(execution.getId());

        assertThat(result.failureReasons()).isEmpty();
    }

    @DisplayName("존재하지 않는 배치를 조회하면 예외가 발생한다.")
    @Test
    void readExecution_notFound() {
        assertThatThrownBy(() -> adminEmailBatchDetailService.readExecution(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(EmailBatchErrorCode.EMAIL_BATCH_NOT_FOUND));
    }

    @DisplayName("배치의 발송 상세 목록을 발송 시각 순으로 조회한다.")
    @Test
    void readSendLogs() {
        EmailBatchExecution execution =
                emailBatchExecutionRepository.save(EmailBatchFixture.completedExecution(1, 1));
        emailSendLogRepository.save(EmailBatchFixture.failureLog(
                execution.getId(), 2L, SENT_AT.plusSeconds(5), "Mailbox full"));
        emailSendLogRepository.save(EmailBatchFixture.successLog(execution.getId(), 1L, SENT_AT.plusSeconds(1)));

        EmailSendLogsResult result = adminEmailBatchDetailService.readSendLogs(
                EmailSendLogSearchCommand.of(execution.getId(), null, 0, 20));

        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.sendLogs())
                .extracting(EmailSendLogResult::userId, EmailSendLogResult::status)
                .containsExactly(
                        tuple(1L, EmailSendStatus.SUCCESS),
                        tuple(2L, EmailSendStatus.FAILURE)
                );
    }

    @DisplayName("상태를 지정하면 해당 상태의 발송 상세만 조회한다.")
    @Test
    void readSendLogs_filterByStatus() {
        EmailBatchExecution execution =
                emailBatchExecutionRepository.save(EmailBatchFixture.completedExecution(1, 1));
        emailSendLogRepository.save(EmailBatchFixture.successLog(execution.getId(), 1L, SENT_AT));
        emailSendLogRepository.save(EmailBatchFixture.failureLog(
                execution.getId(), 2L, SENT_AT, "Mailbox full"));

        EmailSendLogsResult result = adminEmailBatchDetailService.readSendLogs(
                EmailSendLogSearchCommand.of(execution.getId(), EmailSendStatus.FAILURE, 0, 20));

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.sendLogs())
                .extracting(EmailSendLogResult::userId, EmailSendLogResult::failureReason)
                .containsExactly(tuple(2L, "Mailbox full"));
    }

    @DisplayName("존재하지 않는 배치의 발송 상세를 조회하면 예외가 발생한다.")
    @Test
    void readSendLogs_batchNotFound() {
        assertThatThrownBy(() -> adminEmailBatchDetailService.readSendLogs(
                EmailSendLogSearchCommand.of(999L, null, 0, 20)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(EmailBatchErrorCode.EMAIL_BATCH_NOT_FOUND));
    }
}
