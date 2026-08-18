package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.ChoiceDistributionResult;

public record ChoiceDistributionResponse(
        Long choiceId,
        int sequence,
        String content,
        boolean correct,
        long selectedCount,
        double selectedRate
) {

    public static ChoiceDistributionResponse from(ChoiceDistributionResult result) {
        return new ChoiceDistributionResponse(
                result.choiceId(),
                result.sequence(),
                result.content(),
                result.correct(),
                result.selectedCount(),
                result.selectedRate()
        );
    }
}
