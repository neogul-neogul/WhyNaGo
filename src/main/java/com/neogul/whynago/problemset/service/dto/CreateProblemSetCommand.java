package com.neogul.whynago.problemset.service.dto;

public record CreateProblemSetCommand(
        Long userId,
        String name
) {
}
