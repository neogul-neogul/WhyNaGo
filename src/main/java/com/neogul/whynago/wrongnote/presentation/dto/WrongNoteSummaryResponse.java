package com.neogul.whynago.wrongnote.presentation.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteSummaryResult;
import java.time.LocalDateTime;

public record WrongNoteSummaryResponse(
        Long id,
        QuestionType type,
        Category category,
        Difficulty difficulty,
        String title,
        boolean isBookmarked,
        LocalDateTime solvedAt
) {

    public static WrongNoteSummaryResponse from(WrongNoteSummaryResult result) {
        return new WrongNoteSummaryResponse(
                result.id(),
                result.type(),
                result.category(),
                result.difficulty(),
                result.title(),
                result.isBookmarked(),
                result.solvedAt()
        );
    }
}
