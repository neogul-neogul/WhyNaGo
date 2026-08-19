package com.neogul.whynago.solvedsession.domain;

import com.neogul.whynago.common.domain.ElapsedSecondsPolicy;
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

    // 클라이언트가 보고한 소요 시간(초). 미전송이거나 비정상 값이면 null이며 "미측정"을 뜻한다(0초가 아니다).
    private Integer elapsedSeconds;

    @Column(nullable = false)
    private LocalDateTime solvedAt;

    private SolvedMultipleChoice(
            Long solvedSessionId,
            Long userId,
            Long questionId,
            ItemType type,
            int sequence,
            Long userChoiceId,
            Long answerChoiceId,
            boolean isCorrect,
            Integer elapsedSeconds,
            LocalDateTime solvedAt
    ) {
        this.solvedSessionId = solvedSessionId;
        this.userId = userId;
        this.questionId = questionId;
        this.type = type;
        this.sequence = sequence;
        this.userChoiceId = userChoiceId;
        this.answerChoiceId = answerChoiceId;
        this.isCorrect = isCorrect;
        this.elapsedSeconds = elapsedSeconds;
        this.solvedAt = solvedAt;
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
            Integer elapsedSeconds,
            LocalDateTime solvedAt
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
                ElapsedSecondsPolicy.normalize(elapsedSeconds),
                solvedAt
        );
    }
}
