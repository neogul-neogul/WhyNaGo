package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.implement.dto.QuestionSolveStatistics;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.dto.ChoiceSelectionCount;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveSummary;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MultipleChoiceSolveStatisticsReader {

    private final SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    public QuestionSolveStatistics read(Long questionId) {
        QuestionSolveSummary summary = solvedMultipleChoiceRepository.findSolveSummary(questionId);
        Map<Long, Long> selectedCountByChoiceId = solvedMultipleChoiceRepository.countGroupByUserChoice(questionId)
                .stream()
                .collect(Collectors.toMap(
                        ChoiceSelectionCount::getChoiceId,
                        ChoiceSelectionCount::getSelectedCount
                ));

        return new QuestionSolveStatistics(
                summary.getTotalCount(),
                normalize(summary.getCorrectCount()),
                selectedCountByChoiceId
        );
    }

    // 풀이가 한 건도 없으면 sum()이 null이다.
    private long normalize(Long correctCount) {
        return correctCount == null ? 0L : correctCount;
    }
}
