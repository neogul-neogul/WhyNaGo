package com.neogul.whynago.solvedsession.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.dto.ChoiceSelectionCount;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveSummary;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SolvedMultipleChoiceRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Test
    @DisplayName("사용자가 푼 객관식 문제 ID를 중복 없이 조회한다.")
    void findSolvedQuestionIds() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L));
        solvedMultipleChoiceRepository.save(item(10L, 101L));
        solvedMultipleChoiceRepository.save(item(10L, 100L));

        // when
        List<Long> questionIds = solvedMultipleChoiceRepository.findSolvedQuestionIds(10L);

        // then
        assertThat(questionIds).containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    @DisplayName("다른 사용자가 푼 문제 ID는 조회되지 않는다.")
    void findSolvedQuestionIds_otherUser() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L));
        solvedMultipleChoiceRepository.save(item(20L, 200L));

        // when
        List<Long> questionIds = solvedMultipleChoiceRepository.findSolvedQuestionIds(10L);

        // then
        assertThat(questionIds).containsExactly(100L);
    }

    @Test
    @DisplayName("푼 문제가 없으면 빈 목록을 조회한다.")
    void findSolvedQuestionIds_noRecord() {
        // when
        List<Long> questionIds = solvedMultipleChoiceRepository.findSolvedQuestionIds(10L);

        // then
        assertThat(questionIds).isEmpty();
    }

    @Test
    @DisplayName("같은 문제를 여러 사용자가 풀면 풀이 횟수와 정답 수가 합산된다.")
    void findSolveSummary() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L, ItemType.MAIN, 1L, true));
        solvedMultipleChoiceRepository.save(item(20L, 100L, ItemType.MAIN, 2L, false));
        solvedMultipleChoiceRepository.save(item(30L, 100L, ItemType.MAIN, 1L, true));

        // when
        QuestionSolveSummary summary = solvedMultipleChoiceRepository.findSolveSummary(100L);

        // then
        assertThat(summary.getTotalCount()).isEqualTo(3);
        assertThat(summary.getCorrectCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("꼬리질문으로 푼 응답도 문제별 집계에 포함된다.")
    void findSolveSummary_followup() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L, ItemType.MAIN, 1L, true));
        solvedMultipleChoiceRepository.save(item(20L, 100L, ItemType.FOLLOWUP, 1L, true));

        // when
        QuestionSolveSummary summary = solvedMultipleChoiceRepository.findSolveSummary(100L);

        // then
        assertThat(summary.getTotalCount()).isEqualTo(2);
        assertThat(summary.getCorrectCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("다른 문제의 응답은 문제별 집계에서 제외된다.")
    void findSolveSummary_otherQuestion() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L, ItemType.MAIN, 1L, true));
        solvedMultipleChoiceRepository.save(item(10L, 200L, ItemType.MAIN, 1L, true));

        // when
        QuestionSolveSummary summary = solvedMultipleChoiceRepository.findSolveSummary(100L);

        // then
        assertThat(summary.getTotalCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("풀이가 없으면 풀이 횟수는 0이고 정답 수는 null이다.")
    void findSolveSummary_noRecord() {
        // when
        QuestionSolveSummary summary = solvedMultipleChoiceRepository.findSolveSummary(100L);

        // then
        assertThat(summary.getTotalCount()).isZero();
        assertThat(summary.getCorrectCount()).isNull();
    }

    @Test
    @DisplayName("평균 소요 시간은 소요 시간이 수집된 응답만으로 계산한다.")
    void findSolveSummary_averageElapsedSeconds() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L, ItemType.MAIN, 1L, true, 40));
        solvedMultipleChoiceRepository.save(item(20L, 100L, ItemType.MAIN, 1L, true, 60));
        solvedMultipleChoiceRepository.save(item(30L, 100L, ItemType.MAIN, 1L, true, null));

        // when
        QuestionSolveSummary summary = solvedMultipleChoiceRepository.findSolveSummary(100L);

        // then
        assertThat(summary.getTotalCount()).isEqualTo(3);
        assertThat(summary.getAverageElapsedSeconds()).isEqualTo(50.0);
        assertThat(summary.getElapsedSampleCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("소요 시간이 수집된 응답이 없으면 평균은 null이고 표본 수는 0이다.")
    void findSolveSummary_noElapsedSeconds() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L, ItemType.MAIN, 1L, true, null));
        solvedMultipleChoiceRepository.save(item(20L, 100L, ItemType.MAIN, 1L, true, null));

        // when
        QuestionSolveSummary summary = solvedMultipleChoiceRepository.findSolveSummary(100L);

        // then
        assertThat(summary.getTotalCount()).isEqualTo(2);
        assertThat(summary.getAverageElapsedSeconds()).isNull();
        assertThat(summary.getElapsedSampleCount()).isZero();
    }

    @Test
    @DisplayName("보기별 선택 수를 사용자가 고른 보기 기준으로 집계한다.")
    void countGroupByUserChoice() {
        // given
        solvedMultipleChoiceRepository.save(item(10L, 100L, ItemType.MAIN, 1L, false));
        solvedMultipleChoiceRepository.save(item(20L, 100L, ItemType.MAIN, 2L, true));
        solvedMultipleChoiceRepository.save(item(30L, 100L, ItemType.MAIN, 2L, true));
        solvedMultipleChoiceRepository.save(item(40L, 200L, ItemType.MAIN, 3L, true));

        // when
        List<ChoiceSelectionCount> counts = solvedMultipleChoiceRepository.countGroupByUserChoice(100L);

        // then
        assertThat(counts)
                .extracting(ChoiceSelectionCount::getChoiceId, ChoiceSelectionCount::getSelectedCount)
                .containsExactlyInAnyOrder(tuple(1L, 1L), tuple(2L, 2L));
    }

    @Test
    @DisplayName("풀이가 없으면 보기별 선택 수가 빈 목록이다.")
    void countGroupByUserChoice_noRecord() {
        // when
        List<ChoiceSelectionCount> counts = solvedMultipleChoiceRepository.countGroupByUserChoice(100L);

        // then
        assertThat(counts).isEmpty();
    }

    private SolvedMultipleChoice item(Long userId, Long questionId) {
        return item(userId, questionId, ItemType.MAIN, 1L, true);
    }

    private SolvedMultipleChoice item(
            Long userId,
            Long questionId,
            ItemType type,
            Long userChoiceId,
            boolean isCorrect
    ) {
        return item(userId, questionId, type, userChoiceId, isCorrect, null);
    }

    private SolvedMultipleChoice item(
            Long userId,
            Long questionId,
            ItemType type,
            Long userChoiceId,
            boolean isCorrect,
            Integer elapsedSeconds
    ) {
        return SolvedMultipleChoice.create(
                1L,
                userId,
                questionId,
                type,
                1,
                userChoiceId,
                1L,
                isCorrect,
                LocalDateTime.now(),
                elapsedSeconds
        );
    }
}
