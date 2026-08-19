package com.neogul.whynago.admin.implement;

import com.neogul.whynago.admin.implement.dto.ChoiceDistribution;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.solvedsession.implement.dto.QuestionSolveStatistics;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChoiceDistributionCalculator {

    // 아무도 고르지 않은 보기도 0건으로 채운다. 반대로 교체·삭제되어 현재 보기에 없는 응답은
    // 분포에서 빠지지만 전체 풀이 횟수에는 남으므로, 이때 비율 합은 100%에 못 미친다.
    public List<ChoiceDistribution> calculate(List<AnswerChoice> choices, QuestionSolveStatistics statistics) {
        return choices.stream()
                .map(choice -> toDistribution(choice, statistics))
                .toList();
    }

    public ChoiceDistribution findMostChosen(List<ChoiceDistribution> distributions) {
        return distributions.stream()
                .filter(distribution -> distribution.selectedCount() > 0)
                .max(Comparator.comparingLong(ChoiceDistribution::selectedCount)
                        .thenComparing(Comparator.comparingInt(ChoiceDistribution::sequence).reversed()))
                .orElse(null);
    }

    public double rate(long count, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(count * 1000.0 / total) / 10.0;
    }

    private ChoiceDistribution toDistribution(AnswerChoice choice, QuestionSolveStatistics statistics) {
        long selectedCount = statistics.selectedCountByChoiceId().getOrDefault(choice.getId(), 0L);
        return new ChoiceDistribution(
                choice.getId(),
                choice.getSequence(),
                choice.getContent(),
                choice.correct(),
                selectedCount,
                rate(selectedCount, statistics.totalSolveCount())
        );
    }
}
