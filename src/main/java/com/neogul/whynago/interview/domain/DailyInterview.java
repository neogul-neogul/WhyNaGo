package com.neogul.whynago.interview.domain;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.interview.exception.InterviewErrorCode;
import com.neogul.whynago.question.domain.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_daily_interview_user_date", columnNames = {"user_id", "interview_date"}))
public class DailyInterview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate interviewDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private String conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InterviewStatus status;

    private Long solvedSessionId;

    @Column(nullable = false)
    private int focusLossCount;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private DailyInterview(
            Long userId,
            LocalDate interviewDate,
            Category category,
            Long questionId,
            String conversationId,
            InterviewStatus status,
            LocalDateTime startedAt,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.interviewDate = interviewDate;
        this.category = category;
        this.questionId = questionId;
        this.conversationId = conversationId;
        this.status = status;
        this.focusLossCount = 0;
        this.startedAt = startedAt;
        this.createdAt = createdAt;
    }

    public static DailyInterview start(
            Long userId,
            LocalDate interviewDate,
            Category category,
            Long questionId,
            String conversationId,
            LocalDateTime startedAt
    ) {
        return new DailyInterview(
                userId,
                interviewDate,
                category,
                questionId,
                conversationId,
                InterviewStatus.IN_PROGRESS,
                startedAt,
                LocalDateTime.now()
        );
    }

    public void complete(Long solvedSessionId, int focusLossCount, LocalDateTime completedAt) {
        if (!isInProgress()) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_NOT_IN_PROGRESS);
        }
        this.status = InterviewStatus.COMPLETED;
        this.solvedSessionId = solvedSessionId;
        this.focusLossCount = focusLossCount;
        this.completedAt = completedAt;
    }

    public boolean isInProgress() {
        return status == InterviewStatus.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == InterviewStatus.COMPLETED;
    }

    /**
     * 서버 사유로 한 문항도 채점받지 못한 경우에만 취소해 오늘 자리를 돌려준다.
     * 채점 진행도(gradedTurns)는 대화 이력이 갖고 있어 밖에서 받지만, 판정 규칙 자체는 도메인이 소유한다.
     */
    public boolean isCancelable(int gradedTurns) {
        return isInProgress() && gradedTurns == 0;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }
}
