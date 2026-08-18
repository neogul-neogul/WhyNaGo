package com.neogul.whynago.solvedsession.implement.dto;

import java.util.Map;

public record QuestionSolveStatistics(
        long totalSolveCount,
        long correctCount,
        Map<Long, Long> selectedCountByChoiceId
) {
}
