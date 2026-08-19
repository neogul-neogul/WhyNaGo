package com.neogul.whynago.emailbatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_batch_execution")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailBatchExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    @Column(nullable = false)
    private int totalTargetCount;

    @Column(nullable = false)
    private int successCount;

    @Column(nullable = false)
    private int failureCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailBatchStatus status;

    private EmailBatchExecution(
            LocalDateTime executedAt,
            int totalTargetCount,
            int successCount,
            int failureCount,
            EmailBatchStatus status
    ) {
        this.executedAt = executedAt;
        this.totalTargetCount = totalTargetCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.status = status;
    }

    // 배치가 끝나기 전에 저장해야 개별 발송 기록이 참조할 id를 얻으므로, 카운트는 complete에서 확정한다.
    public static EmailBatchExecution start(int totalTargetCount, LocalDateTime executedAt) {
        return new EmailBatchExecution(executedAt, totalTargetCount, 0, 0, EmailBatchStatus.SUCCESS);
    }

    public void complete(int successCount, int failureCount) {
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.status = determineStatus(successCount, failureCount);
    }

    // 보낼 대상이 없는 것은 실패가 아니므로 실패 0건은 성공으로 본다.
    private EmailBatchStatus determineStatus(int successCount, int failureCount) {
        if (failureCount == 0) {
            return EmailBatchStatus.SUCCESS;
        }
        if (successCount == 0) {
            return EmailBatchStatus.FAILED;
        }
        return EmailBatchStatus.PARTIAL_FAILURE;
    }
}
