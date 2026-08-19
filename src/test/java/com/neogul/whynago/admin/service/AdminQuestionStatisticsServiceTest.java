package com.neogul.whynago.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.admin.service.dto.ChoiceDistributionResult;
import com.neogul.whynago.admin.service.dto.MultipleChoiceStatisticsResult;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminQuestionStatisticsServiceTest extends IntegrationTestSupport {

    @Autowired
    private AdminQuestionStatisticsService adminQuestionStatisticsService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Test
    @DisplayName("객관식 문제의 풀이 횟수와 정답률을 조회한다.")
    void readMultipleChoiceStatistics() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        List<AnswerChoice> choices = saveChoices(question.getId());
        solve(question.getId(), choices.get(0), false);
        solve(question.getId(), choices.get(1), true);
        solve(question.getId(), choices.get(1), true);
        solve(question.getId(), choices.get(1), true);

        // when
        MultipleChoiceStatisticsResult result =
                adminQuestionStatisticsService.readMultipleChoiceStatistics(question.getId());

        // then
        assertThat(result.questionId()).isEqualTo(question.getId());
        assertThat(result.totalSolveCount()).isEqualTo(4);
        assertThat(result.correctCount()).isEqualTo(3);
        assertThat(result.correctRate()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("객관식 문제의 보기별 선택 분포를 보기 순서대로 조회한다.")
    void readMultipleChoiceStatistics_distribution() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        List<AnswerChoice> choices = saveChoices(question.getId());
        solve(question.getId(), choices.get(0), false);
        solve(question.getId(), choices.get(1), true);
        solve(question.getId(), choices.get(1), true);
        solve(question.getId(), choices.get(1), true);

        // when
        MultipleChoiceStatisticsResult result =
                adminQuestionStatisticsService.readMultipleChoiceStatistics(question.getId());

        // then
        assertThat(result.choiceDistribution())
                .extracting(
                        ChoiceDistributionResult::sequence,
                        ChoiceDistributionResult::selectedCount,
                        ChoiceDistributionResult::selectedRate
                )
                .containsExactly(
                        tuple(1, 1L, 25.0),
                        tuple(2, 3L, 75.0),
                        tuple(3, 0L, 0.0),
                        tuple(4, 0L, 0.0)
                );
        assertThat(result.mostChosenChoice().sequence()).isEqualTo(2);
        assertThat(result.mostChosenChoice().correct()).isTrue();
    }

    @Test
    @DisplayName("꼬리질문으로 푼 응답도 문제별 통계에 포함된다.")
    void readMultipleChoiceStatistics_followup() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        List<AnswerChoice> choices = saveChoices(question.getId());
        solve(question.getId(), choices.get(1), true, ItemType.MAIN);
        solve(question.getId(), choices.get(1), true, ItemType.FOLLOWUP);

        // when
        MultipleChoiceStatisticsResult result =
                adminQuestionStatisticsService.readMultipleChoiceStatistics(question.getId());

        // then
        assertThat(result.totalSolveCount()).isEqualTo(2);
        assertThat(result.correctRate()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("아직 아무도 풀지 않은 문제는 모든 지표가 0이고 가장 많이 고른 선택지가 없다.")
    void readMultipleChoiceStatistics_noRecord() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        saveChoices(question.getId());

        // when
        MultipleChoiceStatisticsResult result =
                adminQuestionStatisticsService.readMultipleChoiceStatistics(question.getId());

        // then
        assertThat(result.totalSolveCount()).isZero();
        assertThat(result.correctCount()).isZero();
        assertThat(result.correctRate()).isZero();
        assertThat(result.mostChosenChoice()).isNull();
        assertThat(result.choiceDistribution())
                .hasSize(4)
                .extracting(ChoiceDistributionResult::selectedCount)
                .containsOnly(0L);
    }

    @Test
    @DisplayName("평균 소요 시간과 그 표본 수를 조회한다.")
    void readMultipleChoiceStatistics_averageElapsedSeconds() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        List<AnswerChoice> choices = saveChoices(question.getId());
        solve(question.getId(), choices.get(1), true, ItemType.MAIN, 40);
        solve(question.getId(), choices.get(1), true, ItemType.MAIN, 65);
        solve(question.getId(), choices.get(0), false, ItemType.MAIN, null);

        // when
        MultipleChoiceStatisticsResult result =
                adminQuestionStatisticsService.readMultipleChoiceStatistics(question.getId());

        // then
        assertThat(result.totalSolveCount()).isEqualTo(3);
        assertThat(result.averageElapsedSeconds()).isEqualTo(53);
        assertThat(result.elapsedSampleCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("소요 시간이 수집되지 않은 문제는 평균 소요 시간이 없다.")
    void readMultipleChoiceStatistics_noElapsedSeconds() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        List<AnswerChoice> choices = saveChoices(question.getId());
        solve(question.getId(), choices.get(1), true);

        // when
        MultipleChoiceStatisticsResult result =
                adminQuestionStatisticsService.readMultipleChoiceStatistics(question.getId());

        // then
        assertThat(result.totalSolveCount()).isEqualTo(1);
        assertThat(result.averageElapsedSeconds()).isNull();
        assertThat(result.elapsedSampleCount()).isZero();
    }

    @Test
    @DisplayName("서술형 문제의 통계를 조회하면 예외가 발생한다.")
    void readMultipleChoiceStatistics_essayQuestion() {
        // given
        Question question = questionRepository.save(QuestionFixture.essayRoot());

        // when & then
        assertThatThrownBy(() -> adminQuestionStatisticsService.readMultipleChoiceStatistics(question.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_MULTIPLE_CHOICE));
    }

    @Test
    @DisplayName("존재하지 않는 문제의 통계를 조회하면 예외가 발생한다.")
    void readMultipleChoiceStatistics_notFound() {
        // when & then
        assertThatThrownBy(() -> adminQuestionStatisticsService.readMultipleChoiceStatistics(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_FOUND));
    }

    private List<AnswerChoice> saveChoices(Long questionId) {
        return answerChoiceRepository.saveAll(List.of(
                AnswerChoiceFixture.wrong(questionId, 1),
                AnswerChoiceFixture.correct(questionId, 2, null),
                AnswerChoiceFixture.wrong(questionId, 3),
                AnswerChoiceFixture.wrong(questionId, 4)
        ));
    }

    private void solve(Long questionId, AnswerChoice userChoice, boolean isCorrect) {
        solve(questionId, userChoice, isCorrect, ItemType.MAIN);
    }

    private void solve(Long questionId, AnswerChoice userChoice, boolean isCorrect, ItemType type) {
        solve(questionId, userChoice, isCorrect, type, null);
    }

    private void solve(
            Long questionId,
            AnswerChoice userChoice,
            boolean isCorrect,
            ItemType type,
            Integer elapsedSeconds
    ) {
        solvedMultipleChoiceRepository.save(SolvedMultipleChoice.create(
                1L,
                10L,
                questionId,
                type,
                1,
                userChoice.getId(),
                userChoice.getId(),
                isCorrect,
                LocalDateTime.now(),
                elapsedSeconds
        ));
    }
}
