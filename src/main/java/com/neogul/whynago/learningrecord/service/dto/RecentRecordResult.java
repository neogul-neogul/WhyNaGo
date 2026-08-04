package com.neogul.whynago.learningrecord.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import java.time.LocalDateTime;

public record RecentRecordResult(
        Long sessionId,
        QuestionType type,
        Category category,
        int totalCount,
        int correctCount,
        int wrongCount,
        LocalDateTime startedAt,
        LocalDateTime solvedAt
) {

    public static RecentRecordResult of(SolvedSession session, Category category) {
        return new RecentRecordResult(
                session.getId(),
                session.getType(),
                category,
                session.getTotalCount(),
                session.getCorrectCount(),
                session.getTotalCount() - session.getCorrectCount(),
                session.getStartedAt(),
                session.getSolvedAt()
        );
    }
}