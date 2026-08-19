package com.neogul.whynago.emailbatch.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.emailbatch.domain.EmailSendLog;
import com.neogul.whynago.emailbatch.domain.EmailSendStatus;
import com.neogul.whynago.emailbatch.fixture.EmailBatchFixture;
import com.neogul.whynago.emailbatch.infra.dto.FailureReasonCount;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class EmailSendLogRepositoryTest extends RepositoryTestSupport {

    private static final Long BATCH_ID = 1L;
    private static final Long OTHER_BATCH_ID = 2L;
    private static final LocalDateTime SENT_AT = LocalDateTime.of(2026, 8, 19, 21, 0, 0);

    @Autowired
    private EmailSendLogRepository emailSendLogRepository;

    @DisplayName("배치의 발송 기록을 발송 시각 순으로 조회한다.")
    @Test
    void findAllByBatchExecutionIdOrderBySentAtAsc() {
        em.persistAndFlush(EmailBatchFixture.successLog(BATCH_ID, 2L, SENT_AT.plusSeconds(5)));
        em.persistAndFlush(EmailBatchFixture.successLog(BATCH_ID, 1L, SENT_AT.plusSeconds(1)));
        em.persistAndFlush(EmailBatchFixture.successLog(OTHER_BATCH_ID, 3L, SENT_AT));
        em.clear();

        Page<EmailSendLog> found = emailSendLogRepository
                .findAllByBatchExecutionIdOrderBySentAtAsc(BATCH_ID, PageRequest.of(0, 10));

        assertThat(found.getTotalElements()).isEqualTo(2);
        assertThat(found.getContent())
                .extracting(EmailSendLog::getUserId)
                .containsExactly(1L, 2L);
    }

    @DisplayName("상태로 걸러 실패한 발송 기록만 조회한다.")
    @Test
    void findAllByBatchExecutionIdAndStatusOrderBySentAtAsc() {
        em.persistAndFlush(EmailBatchFixture.successLog(BATCH_ID, 1L, SENT_AT.plusSeconds(1)));
        em.persistAndFlush(EmailBatchFixture.failureLog(BATCH_ID, 2L, SENT_AT.plusSeconds(2), "Mailbox full"));
        em.persistAndFlush(EmailBatchFixture.failureLog(BATCH_ID, 3L, SENT_AT.plusSeconds(3), "Invalid address"));
        em.clear();

        Page<EmailSendLog> found = emailSendLogRepository.findAllByBatchExecutionIdAndStatusOrderBySentAtAsc(
                BATCH_ID, EmailSendStatus.FAILURE, PageRequest.of(0, 10));

        assertThat(found.getTotalElements()).isEqualTo(2);
        assertThat(found.getContent())
                .extracting(EmailSendLog::getUserId, EmailSendLog::getFailureReason)
                .containsExactly(
                        tuple(2L, "Mailbox full"),
                        tuple(3L, "Invalid address")
                );
    }

    @DisplayName("실패 사유별 건수를 많은 순으로 집계한다.")
    @Test
    void countGroupByFailureReason() {
        em.persistAndFlush(EmailBatchFixture.successLog(BATCH_ID, 1L, SENT_AT));
        em.persistAndFlush(EmailBatchFixture.failureLog(BATCH_ID, 2L, SENT_AT, "Mailbox full"));
        em.persistAndFlush(EmailBatchFixture.failureLog(BATCH_ID, 3L, SENT_AT, "Invalid address"));
        em.persistAndFlush(EmailBatchFixture.failureLog(BATCH_ID, 4L, SENT_AT, "Invalid address"));
        em.persistAndFlush(EmailBatchFixture.failureLog(OTHER_BATCH_ID, 5L, SENT_AT, "Network error"));
        em.clear();

        List<FailureReasonCount> found =
                emailSendLogRepository.countGroupByFailureReason(BATCH_ID, EmailSendStatus.FAILURE);

        assertThat(found)
                .extracting(FailureReasonCount::getReason, FailureReasonCount::getSendCount)
                .containsExactly(
                        tuple("Invalid address", 2L),
                        tuple("Mailbox full", 1L)
                );
    }
}
