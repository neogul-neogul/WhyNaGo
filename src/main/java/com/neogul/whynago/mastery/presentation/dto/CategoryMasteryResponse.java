package com.neogul.whynago.mastery.presentation.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.service.dto.CategoryMasteryResult;
import com.neogul.whynago.question.domain.Category;
import java.util.List;
import java.util.Map;

public record CategoryMasteryResponse(
        Category category,
        Map<MasteryLevel, Long> levelCounts,
        List<TagMasteryResponse> tags
) {

    public static CategoryMasteryResponse from(CategoryMasteryResult result) {
        return new CategoryMasteryResponse(
                result.category(),
                result.levelCounts(),
                result.tags().stream()
                        .map(TagMasteryResponse::from)
                        .toList()
        );
    }
}
