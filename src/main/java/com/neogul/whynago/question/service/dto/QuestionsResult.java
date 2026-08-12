package com.neogul.whynago.question.service.dto;

import java.util.List;

public record QuestionsResult(
        List<QuestionResult> questions,
        int page,
        int size,
        long totalElements
) {
}
