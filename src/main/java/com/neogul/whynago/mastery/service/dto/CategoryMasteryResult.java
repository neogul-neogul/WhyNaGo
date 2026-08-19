package com.neogul.whynago.mastery.service.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.question.domain.Category;
import java.util.List;
import java.util.Map;

// levelCounts는 그 카테고리에서 각 숙련도를 몇 번 받았는지다. 판정이 없는 숙련도는 키에서 빠진다.
public record CategoryMasteryResult(
        Category category,
        Map<MasteryLevel, Long> levelCounts,
        List<TagMasteryResult> tags
) {
}
