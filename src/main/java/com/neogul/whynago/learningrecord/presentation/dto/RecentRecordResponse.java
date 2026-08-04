package com.neogul.whynago.learningrecord.presentation.dto;

import com.neogul.whynago.learningrecord.service.dto.RecentRecordResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.QuestionType;
import java.time.LocalDateTime;

public record RecentRecordResponse(
        Long sessionId,
        QuestionType type,
        Category category,
        int totalCount,
        int correctCount,
        int wrongCount,
        LocalDateTime startedAt,
        LocalDateTime solvedAt
) {

    public static RecentRecordResponse from(RecentRecordResult result) {
        return new RecentRecordResponse(
                result.sessionId(),
                result.type(),
                result.category(),
                result.totalCount(),
                result.correctCount(),
                result.wrongCount(),
                result.startedAt(),
                result.solvedAt()
        );
    }
}