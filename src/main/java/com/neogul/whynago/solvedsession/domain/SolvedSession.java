package com.neogul.whynago.solvedsession.domain;

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
import com.neogul.whynago.question.domain.QuestionType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SolvedSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    private SessionSource source;

    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private int totalCount;

    private int correctCount;

    private LocalDateTime solvedAt;

    private LocalDateTime createdAt;

    private SolvedSession(
            Long userId,
            QuestionType type,
            SessionSource source,
            SessionStatus status,
            int totalCount,
            int correctCount,
            LocalDateTime solvedAt,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.type = type;
        this.source = source;
        this.status = status;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
        this.solvedAt = solvedAt;
        this.createdAt = createdAt;
    }

    public static SolvedSession completed(
            Long userId,
            QuestionType type,
            SessionSource source,
            int totalCount,
            int correctCount,
            LocalDateTime solvedAt
    ) {
        return new SolvedSession(
                userId,
                type,
                source,
                SessionStatus.COMPLETED,
                totalCount,
                correctCount,
                solvedAt,
                LocalDateTime.now()
        );
    }

    public static SolvedSession abandoned(
            Long userId,
            QuestionType type,
            SessionSource source,
            int totalCount,
            int correctCount,
            LocalDateTime solvedAt
    ) {
        return new SolvedSession(
                userId,
                type,
                source,
                SessionStatus.ABANDONED,
                totalCount,
                correctCount,
                solvedAt,
                LocalDateTime.now()
        );
    }
}
