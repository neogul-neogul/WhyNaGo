package com.neogul.whynago.wrongnote.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.wrongnote.domain.WrongNote;
import java.time.LocalDateTime;

public record WrongNoteSummaryResult(
        Long id,
        QuestionType type,
        Category category,
        Difficulty difficulty,
        String title,
        boolean isBookmarked,
        LocalDateTime solvedAt
) {

    public static WrongNoteSummaryResult from(WrongNote wrongNote, SolvedSession solvedSession, Question rootQuestion) {
        return new WrongNoteSummaryResult(
                wrongNote.getId(),
                solvedSession.getType(),
                rootQuestion.getCategory(),
                rootQuestion.getDifficulty(),
                rootQuestion.getTitle(),
                wrongNote.isBookmarked(),
                solvedSession.getSolvedAt()
        );
    }
}