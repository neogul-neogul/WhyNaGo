package com.neogul.whynago.progress.implement.dto;

import com.neogul.whynago.question.domain.Category;
import java.util.Map;

public record UserProgressAggregate(
        int totalScore,
        int totalQuestionCount,
        int totalCorrectCount,
        Map<Category, Integer> categoryQuestionCounts
) {
}
