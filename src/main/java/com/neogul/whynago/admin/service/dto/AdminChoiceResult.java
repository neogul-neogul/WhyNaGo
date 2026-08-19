package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.question.domain.AnswerChoice;

public record AdminChoiceResult(
        Long id,
        int sequence,
        String content,
        boolean correct,
        String explanation,
        Long relatedQuestionId
) {

    public static AdminChoiceResult from(AnswerChoice answerChoice) {
        return new AdminChoiceResult(
                answerChoice.getId(),
                answerChoice.getSequence(),
                answerChoice.getContent(),
                answerChoice.correct(),
                answerChoice.getExplanation(),
                answerChoice.getRelatedQuestionId()
        );
    }
}
