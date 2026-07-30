package com.neogul.whynago.wrongnote.service.dto;

import com.neogul.whynago.solvedsession.domain.EssaySolved;

public record WrongNoteEssayItemResult(
        int sequence,
        String questionText,
        String userAnswer,
        String feedback,
        String modelAnswer,
        boolean isCorrect
) {

    public static WrongNoteEssayItemResult from(EssaySolved item) {
        return new WrongNoteEssayItemResult(
                item.getSequence(),
                item.getQuestionText(),
                item.getUserAnswer(),
                item.getFeedback(),
                item.getModelAnswer(),
                item.isCorrect()
        );
    }
}