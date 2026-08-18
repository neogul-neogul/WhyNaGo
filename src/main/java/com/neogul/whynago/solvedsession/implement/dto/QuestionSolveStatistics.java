package com.neogul.whynago.solvedsession.implement.dto;

import java.util.Map;

public record QuestionSolveStatistics(
        long totalSolveCount,
        long correctCount,
        Map<Long, Long> selectedCountByChoiceId,
        // 소요 시간이 수집된 응답이 없으면 null이다. 0과 구분해야 하므로 접지 않는다.
        Double averageElapsedSeconds,
        long elapsedSampleCount
) {
}
