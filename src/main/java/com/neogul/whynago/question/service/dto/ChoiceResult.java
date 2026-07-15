package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.question.domain.AnswerChoice;

public record ChoiceResult(
        Long id,
        String content,
        int sequence,
        String explanation,
        Long relatedQuestionId
) {

    public static ChoiceResult from(AnswerChoice answerChoice) {
        return new ChoiceResult(
                answerChoice.getId(),
                answerChoice.getContent(),
                answerChoice.getSequence(),
                answerChoice.getExplanation(),
                answerChoice.getRelatedQuestionId()
        );
    }
}
