package com.neogul.whynago.learningrecord.implement;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.learningrecord.implement.dto.DailyCount;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DailyRecordAggregatorTest {

    private final DailyRecordAggregator dailyRecordAggregator = new DailyRecordAggregator();

    @Test
    @DisplayName("같은 날짜에 완료한 세션들을 세션 수·문항 수로 집계한다.")
    void aggregate() {
        List<SolvedSession> sessions = List.of(
                session(3, LocalDateTime.of(2026, 6, 24, 10, 0)),
                session(2, LocalDateTime.of(2026, 6, 25, 9, 0)),
                session(4, LocalDateTime.of(2026, 6, 25, 20, 0))
        );

        List<DailyCount> result = dailyRecordAggregator.aggregate(sessions);

        assertThat(result).extracting(DailyCount::date)
                .containsExactly(LocalDate.of(2026, 6, 24), LocalDate.of(2026, 6, 25));
        assertThat(result.get(1).sessionCount()).isEqualTo(2);
        assertThat(result.get(1).questionCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("세션이 없으면 빈 목록을 반환한다.")
    void aggregate_empty() {
        List<DailyCount> result = dailyRecordAggregator.aggregate(List.of());

        assertThat(result).isEmpty();
    }

    private SolvedSession session(int totalCount, LocalDateTime solvedAt) {
        return SolvedSession.completed(10L, QuestionType.MULTIPLE_CHOICE, totalCount, totalCount, solvedAt.minusMinutes(5), solvedAt);
    }
}
