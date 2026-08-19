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
public class EssaySolved {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long solvedSessionId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemType type;

    @Column(nullable = false)
    private int sequence;

    // 본질문(MAIN)만 값이 있고, 꼬리질문(FOLLOWUP)은 AI 생성이라 참조할 Question이 없어 null이다.
    private Long questionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String feedback;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String modelAnswer;

    @Column(nullable = false)
    private boolean isCorrect;

    // AI 채점 점수(0~10). 클라이언트가 중계하지 않으면 null이며 "점수 신호 없음"을 뜻한다(0점이 아니다).
    private Integer score;

    // 클라이언트가 보고한 소요 시간(초). 미전송이거나 비정상 값이면 null이며 "미측정"을 뜻한다(0초가 아니다).
    private Integer elapsedSeconds;

    @Column(nullable = false)
    private LocalDateTime solvedAt;

    private EssaySolved(
            Long solvedSessionId,
            Long userId,
            ItemType type,
            int sequence,
            Long questionId,
            String questionText,
            String userAnswer,
            String feedback,
            String modelAnswer,
            boolean isCorrect,
            Integer score,
            Integer elapsedSeconds,
            LocalDateTime solvedAt
    ) {
        this.solvedSessionId = solvedSessionId;
        this.userId = userId;
        this.type = type;
        this.sequence = sequence;
        this.questionId = questionId;
        this.questionText = questionText;
        this.userAnswer = userAnswer;
        this.feedback = feedback;
        this.modelAnswer = modelAnswer;
        this.isCorrect = isCorrect;
        this.score = score;
        this.elapsedSeconds = elapsedSeconds;
        this.solvedAt = solvedAt;
    }

    public static EssaySolved create(
            Long solvedSessionId,
            Long userId,
            ItemType type,
            int sequence,
            Long questionId,
            String questionText,
            String userAnswer,
            String feedback,
            String modelAnswer,
            boolean isCorrect,
            Integer score,
            Integer elapsedSeconds,
            LocalDateTime solvedAt
    ) {
        return new EssaySolved(
                solvedSessionId,
                userId,
                type,
                sequence,
                questionId,
                questionText,
                userAnswer,
                feedback,
                modelAnswer,
                isCorrect,
                EssayScorePolicy.normalize(score),
                ElapsedSecondsPolicy.normalize(elapsedSeconds),
                solvedAt
        );
    }
}
