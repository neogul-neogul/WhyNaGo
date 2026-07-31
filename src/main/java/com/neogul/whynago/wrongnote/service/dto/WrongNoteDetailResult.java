package com.neogul.whynago.wrongnote.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.wrongnote.domain.WrongNote;
import java.time.LocalDateTime;
import java.util.List;

public record WrongNoteDetailResult(
        Long id,
        QuestionType type,
        Category category,
        Difficulty difficulty,
        boolean isBookmarked,
        LocalDateTime solvedAt,
        List<WrongNoteMultipleChoiceItemResult> multipleChoiceItems,
        List<WrongNoteEssayItemResult> essayItems
) {

    public static WrongNoteDetailResult ofMultipleChoice(
            WrongNote wrongNote,
            SolvedSession solvedSession,
            Question rootQuestion,
            List<WrongNoteMultipleChoiceItemResult> items
    ) {
        return new WrongNoteDetailResult(
                wrongNote.getId(),
                solvedSession.getType(),
                rootQuestion.getCategory(),
                rootQuestion.getDifficulty(),
                wrongNote.isBookmarked(),
                solvedSession.getSolvedAt(),
                items,
                null
        );
    }

    public static WrongNoteDetailResult ofEssay(
            WrongNote wrongNote,
            SolvedSession solvedSession,
            Question rootQuestion,
            List<WrongNoteEssayItemResult> items
    ) {
        return new WrongNoteDetailResult(
                wrongNote.getId(),
                solvedSession.getType(),
                rootQuestion.getCategory(),
                rootQuestion.getDifficulty(),
                wrongNote.isBookmarked(),
                solvedSession.getSolvedAt(),
                null,
                items
        );
    }
}