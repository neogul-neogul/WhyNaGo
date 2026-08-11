package com.neogul.whynago.interview.presentation.dto;

import com.neogul.whynago.interview.service.dto.InterviewResultItemDetail;

public record InterviewResultItemResponse(
        int sequence,
        String type,
        String questionText,
        String userAnswer,
        String feedback,
        String modelAnswer,
        boolean isCorrect
) {

    static InterviewResultItemResponse from(InterviewResultItemDetail detail) {
        return new InterviewResultItemResponse(
                detail.sequence(),
                detail.type(),
                detail.questionText(),
                detail.userAnswer(),
                detail.feedback(),
                detail.modelAnswer(),
                detail.isCorrect()
        );
    }
}
