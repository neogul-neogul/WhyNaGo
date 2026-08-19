package com.neogul.whynago.mastery.presentation.dto;

import com.neogul.whynago.mastery.service.dto.MasteryResult;
import java.util.List;

public record MasteryResponse(List<CategoryMasteryResponse> categories) {

    public static MasteryResponse from(MasteryResult result) {
        return new MasteryResponse(result.categories().stream()
                .map(CategoryMasteryResponse::from)
                .toList());
    }
}
