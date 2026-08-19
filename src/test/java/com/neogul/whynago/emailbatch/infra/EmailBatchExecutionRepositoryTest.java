package com.neogul.whynago.emailbatch.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import com.neogul.whynago.emailbatch.fixture.EmailBatchFixture;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class EmailBatchExecutionRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private EmailBatchExecutionRepository emailBatchExecutionRepository;

    @DisplayName("배치 실행 이력을 실행 시각 역순으로 조회한다.")
    @Test
    void findAllByOrderByExecutedAtDesc() {
        em.persistAndFlush(EmailBatchFixture.execution(10, LocalDateTime.of(2026, 8, 17, 21, 0)));
        em.persistAndFlush(EmailBatchFixture.execution(20, LocalDateTime.of(2026, 8, 19, 21, 0)));
        em.persistAndFlush(EmailBatchFixture.execution(30, LocalDateTime.of(2026, 8, 18, 21, 0)));
        em.clear();

        Page<EmailBatchExecution> found =
                emailBatchExecutionRepository.findAllByOrderByExecutedAtDesc(PageRequest.of(0, 10));

        assertThat(found.getContent())
                .extracting(EmailBatchExecution::getExecutedAt)
                .containsExactly(
                        LocalDateTime.of(2026, 8, 19, 21, 0),
                        LocalDateTime.of(2026, 8, 18, 21, 0),
                        LocalDateTime.of(2026, 8, 17, 21, 0)
                );
    }

    @DisplayName("페이지 크기만큼 나눠 조회하고 전체 건수를 함께 반환한다.")
    @Test
    void findAllByOrderByExecutedAtDesc_paging() {
        em.persistAndFlush(EmailBatchFixture.execution(10, LocalDateTime.of(2026, 8, 17, 21, 0)));
        em.persistAndFlush(EmailBatchFixture.execution(20, LocalDateTime.of(2026, 8, 18, 21, 0)));
        em.persistAndFlush(EmailBatchFixture.execution(30, LocalDateTime.of(2026, 8, 19, 21, 0)));
        em.clear();

        Page<EmailBatchExecution> found =
                emailBatchExecutionRepository.findAllByOrderByExecutedAtDesc(PageRequest.of(1, 2));

        assertThat(found.getTotalElements()).isEqualTo(3);
        assertThat(found.getContent())
                .extracting(EmailBatchExecution::getExecutedAt)
                .containsExactly(LocalDateTime.of(2026, 8, 17, 21, 0));
    }

    @DisplayName("완료된 배치는 성공·실패 건수와 상태가 저장된다.")
    @Test
    void save_completedExecution() {
        EmailBatchExecution execution = EmailBatchFixture.completedExecution(338, 2);
        em.persistAndFlush(execution);
        em.clear();

        EmailBatchExecution found = emailBatchExecutionRepository.findById(execution.getId()).orElseThrow();

        assertThat(found.getTotalTargetCount()).isEqualTo(340);
        assertThat(found.getSuccessCount()).isEqualTo(338);
        assertThat(found.getFailureCount()).isEqualTo(2);
    }
}
