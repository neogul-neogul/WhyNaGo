package com.neogul.whynago.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.admin.service.dto.EmailBatchExecutionResult;
import com.neogul.whynago.admin.service.dto.EmailBatchExecutionsResult;
import com.neogul.whynago.admin.service.dto.EmailBatchSearchCommand;
import com.neogul.whynago.emailbatch.domain.EmailBatchStatus;
import com.neogul.whynago.emailbatch.fixture.EmailBatchFixture;
import com.neogul.whynago.emailbatch.infra.EmailBatchExecutionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminEmailBatchListServiceTest extends IntegrationTestSupport {

    @Autowired
    private AdminEmailBatchListService adminEmailBatchListService;

    @Autowired
    private EmailBatchExecutionRepository emailBatchExecutionRepository;

    @DisplayName("배치 실행 이력을 최근 실행 순으로 조회한다.")
    @Test
    void readExecutions() {
        emailBatchExecutionRepository.save(
                EmailBatchFixture.completedExecution(10, 0, LocalDateTime.of(2026, 8, 17, 21, 0)));
        emailBatchExecutionRepository.save(
                EmailBatchFixture.completedExecution(18, 2, LocalDateTime.of(2026, 8, 19, 21, 0)));

        EmailBatchExecutionsResult result =
                adminEmailBatchListService.readExecutions(EmailBatchSearchCommand.of(0, 20));

        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.executions())
                .extracting(EmailBatchExecutionResult::executedAt, EmailBatchExecutionResult::status)
                .containsExactly(
                        tuple(LocalDateTime.of(2026, 8, 19, 21, 0), EmailBatchStatus.PARTIAL_FAILURE),
                        tuple(LocalDateTime.of(2026, 8, 17, 21, 0), EmailBatchStatus.SUCCESS)
                );
    }

    @DisplayName("실행 이력이 없으면 빈 목록을 반환한다.")
    @Test
    void readExecutions_empty() {
        EmailBatchExecutionsResult result =
                adminEmailBatchListService.readExecutions(EmailBatchSearchCommand.of(0, 20));

        assertThat(result.executions()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }
}
