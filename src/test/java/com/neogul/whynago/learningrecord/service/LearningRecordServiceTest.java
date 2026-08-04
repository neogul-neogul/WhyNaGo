package com.neogul.whynago.learningrecord.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.learningrecord.service.dto.DailyRecordCountResult;
import com.neogul.whynago.learningrecord.service.dto.RecentRecordResult;
import com.neogul.whynago.learningrecord.service.dto.StreakResult;
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

class LearningRecordServiceTest extends IntegrationTestSupport {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired
    private LearningRecordService learningRecordService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Test
    @DisplayName("최근 세션을 완료 시각 내림차순으로 size개만 조회하고 본질문 카테고리를 함께 반환한다.")
    void findRecent() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        saveSession(10L, root, LocalDateTime.of(2026, 6, 23, 10, 0), 3, 2);
        SolvedSession latest = saveSession(10L, root, LocalDateTime.of(2026, 6, 25, 10, 0), 2, 2);
        saveSession(20L, root, LocalDateTime.of(2026, 6, 26, 10, 0), 1, 1);

        List<RecentRecordResult> result = learningRecordService.findRecent(10L, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sessionId()).isEqualTo(latest.getId());
        assertThat(result.get(0).type()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(result.get(0).category()).isEqualTo(Category.NETWORK);
        assertThat(result.get(0).totalCount()).isEqualTo(2);
        assertThat(result.get(0).correctCount()).isEqualTo(2);
        assertThat(result.get(0).wrongCount()).isZero();
    }

    @Test
    @DisplayName("연속 학습일과 누적 학습일을 오늘 기준으로 계산한다.")
    void getStreak() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        LocalDate today = LocalDate.now(KST);
        saveSession(10L, root, today.atTime(9, 0), 1, 1);
        saveSession(10L, root, today.minusDays(1).atTime(9, 0), 1, 1);
        saveSession(10L, root, today.minusDays(5).atTime(9, 0), 1, 1);

        StreakResult result = learningRecordService.getStreak(10L);

        assertThat(result.streakDays()).isEqualTo(2);
        assertThat(result.cumulativeDays()).isEqualTo(3);
    }

    @Test
    @DisplayName("학습 기록이 없으면 연속·누적 학습일 모두 0이다.")
    void getStreak_noRecords() {
        StreakResult result = learningRecordService.getStreak(999L);

        assertThat(result.streakDays()).isZero();
        assertThat(result.cumulativeDays()).isZero();
    }

    @Test
    @DisplayName("지정한 기간 내 일자별 세션 수·문항 수를 집계한다.")
    void findDailyCounts() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        saveSession(10L, root, LocalDateTime.of(2026, 6, 24, 9, 0), 3, 2);
        saveSession(10L, root, LocalDateTime.of(2026, 6, 24, 20, 0), 2, 1);
        saveSession(10L, root, LocalDateTime.of(2026, 6, 20, 9, 0), 1, 1);

        List<DailyRecordCountResult> result = learningRecordService.findDailyCounts(
                10L, LocalDate.of(2026, 6, 23), LocalDate.of(2026, 6, 25));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 6, 24));
        assertThat(result.get(0).sessionCount()).isEqualTo(2);
        assertThat(result.get(0).questionCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("from이 to보다 늦으면 빈 목록을 반환한다.")
    void findDailyCounts_invalidRange() {
        List<DailyRecordCountResult> result = learningRecordService.findDailyCounts(
                10L, LocalDate.of(2026, 6, 25), LocalDate.of(2026, 6, 20));

        assertThat(result).isEmpty();
    }

    private SolvedSession saveSession(Long userId, Question root, LocalDateTime solvedAt, int totalCount, int correctCount) {
        SolvedSession session = solvedSessionRepository.save(
                SolvedSession.completed(userId, QuestionType.MULTIPLE_CHOICE, totalCount, correctCount, solvedAt.minusMinutes(5), solvedAt));
        AnswerChoice choice = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));
        solvedMultipleChoiceRepository.save(SolvedMultipleChoice.create(
                session.getId(), userId, root.getId(), ItemType.MAIN, 1,
                choice.getId(), choice.getId(), true, solvedAt));
        return session;
    }
}