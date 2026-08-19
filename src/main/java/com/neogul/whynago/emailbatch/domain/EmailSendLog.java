package com.neogul.whynago.emailbatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "email_send_log",
        indexes = @Index(name = "idx_email_send_log_batch_execution_id", columnList = "batchExecutionId")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailSendLog {

    private static final int MAX_FAILURE_REASON_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long batchExecutionId;

    @Column(nullable = false)
    private Long userId;

    // 사용자가 이후 이메일을 바꿔도 "그때 어디로 보냈는지"는 바뀌면 안 되므로 발송 시점 값을 스냅샷으로 남긴다.
    // 대상 조회 자체가 실패하면(탈퇴 등) 주소를 알 수 없으므로 null을 허용한다.
    private String recipientEmail;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailSendStatus status;

    private String failureReason;

    private EmailSendLog(
            Long batchExecutionId,
            Long userId,
            String recipientEmail,
            LocalDateTime sentAt,
            EmailSendStatus status,
            String failureReason
    ) {
        this.batchExecutionId = batchExecutionId;
        this.userId = userId;
        this.recipientEmail = recipientEmail;
        this.sentAt = sentAt;
        this.status = status;
        this.failureReason = failureReason;
    }

    public static EmailSendLog success(
            Long batchExecutionId,
            Long userId,
            String recipientEmail,
            LocalDateTime sentAt
    ) {
        return new EmailSendLog(batchExecutionId, userId, recipientEmail, sentAt, EmailSendStatus.SUCCESS, null);
    }

    public static EmailSendLog failure(
            Long batchExecutionId,
            Long userId,
            String recipientEmail,
            LocalDateTime sentAt,
            String failureReason
    ) {
        return new EmailSendLog(
                batchExecutionId,
                userId,
                recipientEmail,
                sentAt,
                EmailSendStatus.FAILURE,
                truncate(failureReason)
        );
    }

    // 실패 사유는 예외 메시지에서 오므로 길이를 예측할 수 없다. 이력을 남기는 것이 목적이라 잘라서라도 저장한다.
    private static String truncate(String failureReason) {
        if (failureReason == null || failureReason.length() <= MAX_FAILURE_REASON_LENGTH) {
            return failureReason;
        }
        return failureReason.substring(0, MAX_FAILURE_REASON_LENGTH);
    }
}
