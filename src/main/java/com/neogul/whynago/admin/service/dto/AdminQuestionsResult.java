package com.neogul.whynago.admin.service.dto;

import java.util.List;

public record AdminQuestionsResult(
        List<AdminQuestionResult> questions,
        int page,
        int size,
        long totalElements
) {
}
