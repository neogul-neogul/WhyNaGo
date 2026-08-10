package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.interview.implement.dto.InterviewResultItem;

public record InterviewResultItemDetail(
        int sequence,
        String type,
        String questionText,
        String userAnswer,
        String feedback,
        String modelAnswer,
        boolean isCorrect
) {

    public static InterviewResultItemDetail from(InterviewResultItem item) {
        return new InterviewResultItemDetail(
                item.sequence(),
                item.type().name(),
                item.questionText(),
                item.userAnswer(),
                item.feedback(),
                item.modelAnswer(),
                item.isCorrect()
        );
    }
}
