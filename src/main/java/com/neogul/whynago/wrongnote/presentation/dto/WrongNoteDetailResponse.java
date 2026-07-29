package com.neogul.whynago.wrongnote.presentation.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteDetailResult;
import java.time.LocalDateTime;
import java.util.List;

public record WrongNoteDetailResponse(
        Long id,
        QuestionType type,
        Category category,
        Difficulty difficulty,
        boolean isBookmarked,
        LocalDateTime solvedAt,
        List<WrongNoteMultipleChoiceItemResponse> multipleChoiceItems,
        List<WrongNoteEssayItemResponse> essayItems
) {

    public static WrongNoteDetailResponse from(WrongNoteDetailResult result) {
        return new WrongNoteDetailResponse(
                result.id(),
                result.type(),
                result.category(),
                result.difficulty(),
                result.isBookmarked(),
                result.solvedAt(),
                result.multipleChoiceItems() == null
                        ? null
                        : result.multipleChoiceItems().stream()
                                .map(WrongNoteMultipleChoiceItemResponse::from)
                                .toList(),
                result.essayItems() == null
                        ? null
                        : result.essayItems().stream()
                                .map(WrongNoteEssayItemResponse::from)
                                .toList()
        );
    }
}
