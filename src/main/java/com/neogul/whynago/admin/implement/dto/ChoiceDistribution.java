package com.neogul.whynago.admin.implement.dto;

public record ChoiceDistribution(
        Long choiceId,
        int sequence,
        String content,
        boolean correct,
        long selectedCount,
        double selectedRate
) {
}
