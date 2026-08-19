package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.question.implement.QuestionStatWriter;
import com.neogul.whynago.question.implement.dto.QuestionStatSnapshot;
import com.neogul.whynago.solvedsession.implement.dto.QuestionSolveStat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 풀이 이력 전량을 문항 단위로 집계해 question_stat을 덮어쓴다.
// 사용자 무관 전역 집계이므로 조회 시점에 계산하지 않고 배치 테이블로 둔다.
@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionStatAggregator {

    private final QuestionSolveStatReader questionSolveStatReader;
    private final QuestionStatWriter questionStatWriter;
    private final Clock clock;

    public int aggregateAll() {
        List<QuestionStatSnapshot> snapshots = merge(questionSolveStatReader.readAll());
        questionStatWriter.upsertAll(snapshots, LocalDateTime.now(clock));
        log.info("문항 통계 집계 완료 - questionCount={}", snapshots.size());
        return snapshots.size();
    }

    private List<QuestionStatSnapshot> merge(List<QuestionSolveStat> stats) {
        Map<Long, List<QuestionSolveStat>> byQuestion = new LinkedHashMap<>();
        for (QuestionSolveStat stat : stats) {
            byQuestion.computeIfAbsent(stat.questionId(), questionId -> new ArrayList<>()).add(stat);
        }
        return byQuestion.values().stream()
                .map(QuestionStatAggregator::toSnapshot)
                .toList();
    }

    private static QuestionStatSnapshot toSnapshot(List<QuestionSolveStat> stats) {
        long solvedCount = stats.stream().mapToLong(QuestionSolveStat::solvedCount).sum();
        long correctCount = stats.stream().mapToLong(QuestionSolveStat::correctCount).sum();

        return new QuestionStatSnapshot(
                stats.get(0).questionId(),
                averageElapsedSeconds(stats),
                (double) correctCount / solvedCount,
                (int) solvedCount
        );
    }

    // 평균의 평균은 표본 수가 다르면 틀린 값이 되지만, 여기서 합칠 대상은 같은 문항의
    // 객관식·서술형 이력이라 실제로는 한쪽만 존재한다. 그래도 값이 왜곡되지 않게 단순 평균이 아니라
    // 표본이 있는 집계만 골라 평균한다.
    private static Integer averageElapsedSeconds(List<QuestionSolveStat> stats) {
        OptionalDouble average = stats.stream()
                .map(QuestionSolveStat::avgElapsedSeconds)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();

        if (average.isEmpty()) {
            return null;
        }
        return (int) Math.round(average.getAsDouble());
    }
}
