package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.admin.implement.dto.ChoiceDistribution;
import com.neogul.whynago.solvedsession.implement.dto.QuestionSolveStatistics;
import java.util.List;

public record MultipleChoiceStatisticsResult(
        Long questionId,
        long totalSolveCount,
        long correctCount,
        double correctRate,
        Integer averageElapsedSeconds,
        long elapsedSampleCount,
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
                roundSeconds(statistics.averageElapsedSeconds()),
                statistics.elapsedSampleCount(),
                mostChosenChoice == null ? null : ChoiceDistributionResult.from(mostChosenChoice),
                distributions.stream()
                        .map(ChoiceDistributionResult::from)
                        .toList()
        );
    }

    // 초 단위 소수점은 의미가 없어 반올림한다. 수집된 값이 없으면 0이 아니라 null로 남긴다.
    private static Integer roundSeconds(Double averageElapsedSeconds) {
        if (averageElapsedSeconds == null) {
            return null;
        }
        return (int) Math.round(averageElapsedSeconds);
    }
}
