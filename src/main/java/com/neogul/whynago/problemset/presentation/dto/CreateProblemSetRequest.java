package com.neogul.whynago.problemset.presentation.dto;

import com.neogul.whynago.problemset.service.dto.CreateProblemSetCommand;
import jakarta.validation.constraints.NotBlank;

public record CreateProblemSetRequest(
        @NotBlank String name
) {

    public CreateProblemSetCommand toCommand(Long userId) {
        return new CreateProblemSetCommand(userId, name);
    }
}
