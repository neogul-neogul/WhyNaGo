package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.implement.dto.QuestionSolveStatistics;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.dto.ChoiceSelectionCount;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveCount;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveSummary;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
                selectedCountByChoiceId,
                summary.getAverageElapsedSeconds(),
                summary.getElapsedSampleCount()
        );
    }

    // 풀이가 한 건도 없으면 sum()이 null이다.
    private long normalize(Long correctCount) {
        return correctCount == null ? 0L : correctCount;
    }

    // 관리자 문제 목록에서 여러 문제의 풀이수·정답률 컬럼을 한 번에 채울 때 쓴다.
    public Map<Long, QuestionSolveCount> readBulk(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return solvedMultipleChoiceRepository.countGroupByQuestion(questionIds).stream()
                .collect(Collectors.toMap(QuestionSolveCount::getQuestionId, Function.identity()));
    }
}
