package com.neogul.whynago.solvedsession.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SolvedMultipleChoice {

    private static final int MAX_ELAPSED_SECONDS = 3600;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long solvedSessionId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType type;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private Long userChoiceId;

    @Column(nullable = false)
    private Long answerChoiceId;

    @Column(nullable = false)
    private boolean isCorrect;

    @Column(nullable = false)
    private LocalDateTime solvedAt;

    // 문항이 표시된 시점부터 채점을 누른 시점까지. 수집 전에 쌓인 기록은 null이라 집계에서 빠진다.
    private Integer elapsedSeconds;

    private SolvedMultipleChoice(
            Long solvedSessionId,
            Long userId,
            Long questionId,
            ItemType type,
            int sequence,
            Long userChoiceId,
            Long answerChoiceId,
            boolean isCorrect,
            LocalDateTime solvedAt,
            Integer elapsedSeconds
    ) {
        this.solvedSessionId = solvedSessionId;
        this.userId = userId;
        this.questionId = questionId;
        this.type = type;
        this.sequence = sequence;
        this.userChoiceId = userChoiceId;
        this.answerChoiceId = answerChoiceId;
        this.isCorrect = isCorrect;
        this.solvedAt = solvedAt;
        this.elapsedSeconds = elapsedSeconds;
    }

    public static SolvedMultipleChoice create(
            Long solvedSessionId,
            Long userId,
            Long questionId,
            ItemType type,
            int sequence,
            Long userChoiceId,
            Long answerChoiceId,
            boolean isCorrect,
            LocalDateTime solvedAt
    ) {
        return create(
                solvedSessionId,
                userId,
                questionId,
                type,
                sequence,
                userChoiceId,
                answerChoiceId,
                isCorrect,
                solvedAt,
                null
        );
    }

    public static SolvedMultipleChoice create(
            Long solvedSessionId,
            Long userId,
            Long questionId,
            ItemType type,
            int sequence,
            Long userChoiceId,
            Long answerChoiceId,
            boolean isCorrect,
            LocalDateTime solvedAt,
            Integer elapsedSeconds
    ) {
        return new SolvedMultipleChoice(
                solvedSessionId,
                userId,
                questionId,
                type,
                sequence,
                userChoiceId,
                answerChoiceId,
                isCorrect,
                solvedAt,
                normalizeElapsedSeconds(elapsedSeconds)
        );
    }

    // 문제를 띄워놓고 자리를 비운 시간은 소요 시간이 아니다. 세션 저장은 살리고 지표에서만 뺀다.
    private static Integer normalizeElapsedSeconds(Integer elapsedSeconds) {
        if (elapsedSeconds == null || elapsedSeconds > MAX_ELAPSED_SECONDS) {
            return null;
        }
        return elapsedSeconds;
    }
}
