package com.neogul.whynago.wrongnote.presentation.dto;

import com.neogul.whynago.wrongnote.service.dto.WrongNoteChoiceResult;

public record WrongNoteChoiceResponse(
        Long id,
        String content,
        int sequence,
        boolean isCorrect
) {

    static WrongNoteChoiceResponse from(WrongNoteChoiceResult result) {
        return new WrongNoteChoiceResponse(
                result.id(),
                result.content(),
                result.sequence(),
                result.isCorrect()
        );
    }
}
