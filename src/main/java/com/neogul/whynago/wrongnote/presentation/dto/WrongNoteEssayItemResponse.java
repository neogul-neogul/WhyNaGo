package com.neogul.whynago.wrongnote.presentation.dto;

import com.neogul.whynago.wrongnote.service.dto.WrongNoteEssayItemResult;

public record WrongNoteEssayItemResponse(
        int sequence,
        String questionText,
        String userAnswer,
        String feedback,
        String modelAnswer,
        boolean isCorrect
) {

    static WrongNoteEssayItemResponse from(WrongNoteEssayItemResult result) {
        return new WrongNoteEssayItemResponse(
                result.sequence(),
                result.questionText(),
                result.userAnswer(),
                result.feedback(),
                result.modelAnswer(),
                result.isCorrect()
        );
    }
}
