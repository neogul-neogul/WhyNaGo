package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.admin.implement.dto.ChoiceDistribution;

public record ChoiceDistributionResult(
        Long choiceId,
        int sequence,
        String content,
        boolean correct,
        long selectedCount,
        double selectedRate
) {

    public static ChoiceDistributionResult from(ChoiceDistribution distribution) {
        return new ChoiceDistributionResult(
                distribution.choiceId(),
                distribution.sequence(),
                distribution.content(),
                distribution.correct(),
                distribution.selectedCount(),
                distribution.selectedRate()
        );
    }
}
