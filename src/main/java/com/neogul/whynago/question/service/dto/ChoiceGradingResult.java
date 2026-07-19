package com.neogul.whynago.question.service.dto;

import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;

public record ChoiceGradingResult(
        boolean correct,
        Long correctChoiceId,
        String explanation,
        String choiceExplanation,
        QuestionResult nextQuestion
) {

    public static ChoiceGradingResult of(
            Question question,
            AnswerChoice chosenChoice,
            AnswerChoice correctChoice,
            QuestionResult nextQuestion
    ) {
        return new ChoiceGradingResult(
                chosenChoice.correct(),
                correctChoice.getId(),
                question.getExplanation(),
                chosenChoice.correct() ? null : chosenChoice.getExplanation(),
                nextQuestion
        );
    }
}
