package com.neogul.whynago.wrongnote.service.dto;

import com.neogul.whynago.question.domain.AnswerChoice;

public record WrongNoteChoiceResult(
        Long id,
        String content,
        int sequence,
        boolean isCorrect
) {

    public static WrongNoteChoiceResult from(AnswerChoice answerChoice) {
        return new WrongNoteChoiceResult(
                answerChoice.getId(),
                answerChoice.getContent(),
                answerChoice.getSequence(),
                answerChoice.correct()
        );
    }
}
