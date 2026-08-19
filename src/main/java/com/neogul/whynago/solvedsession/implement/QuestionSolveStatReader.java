package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.implement.dto.QuestionSolveStat;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveCount;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionSolveStatReader {

    private final SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;
    private final EssaySolvedRepository essaySolvedRepository;

    // 두 이력 테이블을 각각 집계해 이어 붙인다. 한 문항이 양쪽에 모두 나타나는 일은 없지만,
    // 병합은 호출자(QuestionStatAggregator)가 questionId 기준으로 처리한다.
    public List<QuestionSolveStat> readAll() {
        return Stream.concat(
                        solvedMultipleChoiceRepository.aggregateByQuestion().stream(),
                        essaySolvedRepository.aggregateByQuestion().stream()
                )
                .map(QuestionSolveStatReader::toStat)
                .toList();
    }

    private static QuestionSolveStat toStat(QuestionSolveCount count) {
        return new QuestionSolveStat(
                count.getQuestionId(),
                count.getSolvedCount(),
                count.getCorrectCount(),
                count.getAvgElapsedSeconds()
        );
    }
}
