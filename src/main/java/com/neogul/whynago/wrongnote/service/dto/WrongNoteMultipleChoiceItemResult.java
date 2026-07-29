package com.neogul.whynago.wrongnote.service.dto;

import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import java.util.List;

public record WrongNoteMultipleChoiceItemResult(
        int sequence,
        Long questionId,
        String title,
        String content,
        List<WrongNoteChoiceResult> choices,
        Long userChoiceId,
        Long correctChoiceId,
        boolean isCorrect,
        String explanation,
        String choiceExplanation
) {

    public static WrongNoteMultipleChoiceItemResult from(
            SolvedMultipleChoice item,
            Question question,
            List<AnswerChoice> choices
    ) {
        AnswerChoice userChoice = choices.stream()
                .filter(choice -> choice.getId().equals(item.getUserChoiceId()))
                .findFirst()
                .orElseThrow();

        return new WrongNoteMultipleChoiceItemResult(
                item.getSequence(),
                question.getId(),
                question.getTitle(),
                question.getContent(),
                choices.stream().map(WrongNoteChoiceResult::from).toList(),
                item.getUserChoiceId(),
                item.getAnswerChoiceId(),
                item.isCorrect(),
                question.getExplanation(),
                userChoice.correct() ? null : userChoice.getExplanation()
        );
    }
}