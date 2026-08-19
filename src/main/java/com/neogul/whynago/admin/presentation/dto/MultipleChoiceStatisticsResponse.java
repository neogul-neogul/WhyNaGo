package com.neogul.whynago.admin.presentation.dto;

import com.neogul.whynago.admin.service.dto.MultipleChoiceStatisticsResult;
import java.util.List;

public record MultipleChoiceStatisticsResponse(
        Long questionId,
        long totalSolveCount,
        long correctCount,
        double correctRate,
        Integer averageElapsedSeconds,
        long elapsedSampleCount,
        ChoiceDistributionResponse mostChosenChoice,
        List<ChoiceDistributionResponse> choiceDistribution
) {

    public static MultipleChoiceStatisticsResponse from(MultipleChoiceStatisticsResult result) {
        return new MultipleChoiceStatisticsResponse(
                result.questionId(),
                result.totalSolveCount(),
                result.correctCount(),
                result.correctRate(),
                result.averageElapsedSeconds(),
                result.elapsedSampleCount(),
                result.mostChosenChoice() == null ? null : ChoiceDistributionResponse.from(result.mostChosenChoice()),
                result.choiceDistribution().stream()
                        .map(ChoiceDistributionResponse::from)
                        .toList()
        );
    }
}
