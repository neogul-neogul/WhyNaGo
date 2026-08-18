package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.admin.implement.dto.ChoiceDistribution;
import com.neogul.whynago.solvedsession.implement.dto.QuestionSolveStatistics;
import java.util.List;

public record MultipleChoiceStatisticsResult(
        Long questionId,
        long totalSolveCount,
        long correctCount,
        double correctRate,
        ChoiceDistributionResult mostChosenChoice,
        List<ChoiceDistributionResult> choiceDistribution
) {

    public static MultipleChoiceStatisticsResult of(
            Long questionId,
            QuestionSolveStatistics statistics,
            double correctRate,
            List<ChoiceDistribution> distributions,
            ChoiceDistribution mostChosenChoice
    ) {
        return new MultipleChoiceStatisticsResult(
                questionId,
                statistics.totalSolveCount(),
                statistics.correctCount(),
                correctRate,
                mostChosenChoice == null ? null : ChoiceDistributionResult.from(mostChosenChoice),
                distributions.stream()
                        .map(ChoiceDistributionResult::from)
                        .toList()
        );
    }
}
