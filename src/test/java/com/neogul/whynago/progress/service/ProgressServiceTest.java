package com.neogul.whynago.progress.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.interview.service.InterviewService;
import com.neogul.whynago.interview.service.dto.CompleteInterviewCommand;
import com.neogul.whynago.interview.service.dto.InterviewAnswerSnapshotCommand;
import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.service.dto.ProgressDetailResult;
import com.neogul.whynago.progress.service.dto.ProgressSummaryResult;
import com.neogul.whynago.progress.service.dto.TierRange;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProgressServiceTest extends IntegrationTestSupport {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Long USER_ID = 10L;
    private static final LocalDateTime SOLVED_AT = LocalDateTime.of(2026, 6, 25, 10, 0);

    @Autowired
    private ProgressService progressService;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Test
    @DisplayName("풀이 기록이 없으면 브론즈 티어이고 다음 티어까지 58점이 필요하다.")
    void getDetail_noRecords() {
        ProgressDetailResult result = progressService.getDetail(USER_ID);

        assertThat(result.score()).isZero();
        assertThat(result.tier()).isEqualTo(Tier.BRONZE);
        assertThat(result.nextTier()).isEqualTo(Tier.SILVER);
        assertThat(result.scoreToNextTier()).isEqualTo(58);
        assertThat(result.totalQuestionCount()).isZero();
        assertThat(result.categories()).hasSize(Category.values().length);
        assertThat(result.categories())
                .allSatisfy(category -> {
                    assertThat(category.solvedCount()).isZero();
                    assertThat(category.correctCount()).isZero();
                    assertThat(category.score()).isZero();
                });
    }

    @Test
    @DisplayName("티어 구간표와 표시 상한을 함께 반환한다.")
    void getDetail_tierRanges() {
        ProgressDetailResult result = progressService.getDetail(USER_ID);

        assertThat(result.maxScore()).isEqualTo(700);
        assertThat(result.tiers())
                .extracting(TierRange::tier, TierRange::minScore)
                .containsExactly(
                        tuple(Tier.BRONZE, 0),
                        tuple(Tier.SILVER, 58),
                        tuple(Tier.GOLD, 198),
                        tuple(Tier.PLATINUM, 420),
                        tuple(Tier.DIAMOND, 677)
                );
    }

    @Test
    @DisplayName("만점 세션으로 얻은 점수에 맞는 티어와 다음 티어까지 필요한 점수를 계산한다.")
    void getDetail_withScore() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        SolvedSession session = solvedSessionRepository.save(
                SolvedSession.completed(USER_ID, QuestionType.MULTIPLE_CHOICE, 1, 1, SOLVED_AT.minusMinutes(5), SOLVED_AT));
        AnswerChoice choice = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));
        solvedMultipleChoiceRepository.save(SolvedMultipleChoice.create(
                session.getId(), USER_ID, root.getId(), ItemType.MAIN, 1, choice.getId(), choice.getId(), true, SOLVED_AT));

        ProgressDetailResult result = progressService.getDetail(USER_ID);

        assertThat(result.score()).isEqualTo(2);
        assertThat(result.tier()).isEqualTo(Tier.BRONZE);
        assertThat(result.nextTier()).isEqualTo(Tier.SILVER);
        assertThat(result.scoreToNextTier()).isEqualTo(56);
        assertThat(result.totalQuestionCount()).isEqualTo(1);
        assertThat(result.categories())
                .filteredOn(category -> category.category() == Category.NETWORK)
                .singleElement()
                .satisfies(network -> {
                    assertThat(network.totalCount()).isEqualTo(1);
                    assertThat(network.solvedCount()).isEqualTo(1);
                    assertThat(network.correctCount()).isEqualTo(1);
                    assertThat(network.score()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("풀이·면접 기록이 없으면 상단 통계는 전부 0이다.")
    void getSummary_noRecords() {
        ProgressSummaryResult result = progressService.getSummary(USER_ID);

        assertThat(result.cumulativeDays()).isZero();
        assertThat(result.streakDays()).isZero();
        assertThat(result.totalQuestionCount()).isZero();
        assertThat(result.totalCorrectCount()).isZero();
        assertThat(result.totalWrongCount()).isZero();
        assertThat(result.completedInterviewCount()).isZero();
    }

    @Test
    @DisplayName("문제 풀이와 1일1면접 기록을 종합해 상단 통계를 계산한다.")
    void getSummary_withRecords() {
        LocalDateTime solvedAt = LocalDate.now(KST).atTime(10, 0);
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        SolvedSession session = solvedSessionRepository.save(
                SolvedSession.completed(USER_ID, QuestionType.MULTIPLE_CHOICE, 1, 1, solvedAt.minusMinutes(5), solvedAt));
        AnswerChoice choice = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));
        solvedMultipleChoiceRepository.save(SolvedMultipleChoice.create(
                session.getId(), USER_ID, root.getId(), ItemType.MAIN, 1, choice.getId(), choice.getId(), true, solvedAt));

        questionRepository.save(QuestionFixture.essayRoot());
        Long interviewId = interviewService.start(USER_ID).interviewId();
        interviewService.complete(USER_ID, interviewId, completeCommand(true, false, true));

        ProgressSummaryResult result = progressService.getSummary(USER_ID);

        assertThat(result.cumulativeDays()).isEqualTo(1);
        assertThat(result.streakDays()).isEqualTo(1);
        assertThat(result.totalQuestionCount()).isEqualTo(4); // 객관식 1 + 면접 3
        assertThat(result.totalCorrectCount()).isEqualTo(3); // 객관식 1 + 면접 2
        assertThat(result.totalWrongCount()).isEqualTo(1);
        assertThat(result.completedInterviewCount()).isEqualTo(1);
    }

    private CompleteInterviewCommand completeCommand(boolean first, boolean second, boolean third) {
        return new CompleteInterviewCommand(
                snapshot("본질문", first),
                List.of(snapshot("꼬리질문1", second), snapshot("꼬리질문2", third)),
                0
        );
    }

    private InterviewAnswerSnapshotCommand snapshot(String questionText, boolean isCorrect) {
        return new InterviewAnswerSnapshotCommand(questionText, "답변", "피드백", "모범답안", isCorrect);
    }
}
