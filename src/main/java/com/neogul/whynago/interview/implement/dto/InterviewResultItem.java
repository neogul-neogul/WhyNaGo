package com.neogul.whynago.interview.implement.dto;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;

public record InterviewResultItem(
        int sequence,
        ItemType type,
        String questionText,
        String userAnswer,
        String feedback,
        String modelAnswer,
        boolean isCorrect
) {

    public static InterviewResultItem from(EssaySolved essaySolved) {
        return new InterviewResultItem(
                essaySolved.getSequence(),
                essaySolved.getType(),
                essaySolved.getQuestionText(),
                essaySolved.getUserAnswer(),
                essaySolved.getFeedback(),
                essaySolved.getModelAnswer(),
                essaySolved.isCorrect()
        );
    }
}
