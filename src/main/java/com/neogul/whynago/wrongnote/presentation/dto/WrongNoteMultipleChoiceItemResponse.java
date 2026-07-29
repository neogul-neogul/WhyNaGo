package com.neogul.whynago.wrongnote.presentation.dto;

import com.neogul.whynago.wrongnote.service.dto.WrongNoteMultipleChoiceItemResult;
import java.util.List;

public record WrongNoteMultipleChoiceItemResponse(
        int sequence,
        Long questionId,
        String title,
        String content,
        List<WrongNoteChoiceResponse> choices,
        Long userChoiceId,
        Long correctChoiceId,
        boolean isCorrect,
        String explanation,
        String choiceExplanation
) {

    static WrongNoteMultipleChoiceItemResponse from(WrongNoteMultipleChoiceItemResult result) {
        return new WrongNoteMultipleChoiceItemResponse(
                result.sequence(),
                result.questionId(),
                result.title(),
                result.content(),
                result.choices().stream()
                        .map(WrongNoteChoiceResponse::from)
                        .toList(),
                result.userChoiceId(),
                result.correctChoiceId(),
                result.isCorrect(),
                result.explanation(),
                result.choiceExplanation()
        );
    }
}
