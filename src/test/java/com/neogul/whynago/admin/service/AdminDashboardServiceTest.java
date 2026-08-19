package com.neogul.whynago.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.admin.domain.DashboardAlertType;
import com.neogul.whynago.admin.implement.dto.DashboardAlert;
import com.neogul.whynago.admin.service.dto.DashboardResult;
import com.neogul.whynago.fixture.DailyInterviewFixture;
import com.neogul.whynago.interview.domain.DailyInterviewQuestion;
import com.neogul.whynago.interview.infra.DailyInterviewQuestionRepository;
import com.neogul.whynago.interview.infra.DailyInterviewRepository;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.user.fixture.UserFixture;
import com.neogul.whynago.user.infra.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminDashboardServiceTest extends IntegrationTestSupport {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Autowired
    private DailyInterviewRepository dailyInterviewRepository;

    @Autowired
    private DailyInterviewQuestionRepository dailyInterviewQuestionRepository;

    @Test
    @DisplayName("전체 회원 수와 오늘·전일 가입자 수를 함께 조회한다.")
    void readDashboard_memberCount() {
        LocalDate today = LocalDate.now(KST);
        saveUser("today1", today.atTime(9, 0));
        saveUser("today2", today.atTime(23, 30));
        saveUser("yester1", today.minusDays(1).atTime(9, 0));

        DashboardResult result = adminDashboardService.readDashboard();

        assertThat(result.totalMemberCount()).isEqualTo(3);
        assertThat(result.todaySignUpCount().current()).isEqualTo(2);
        assertThat(result.todaySignUpCount().previous()).isEqualTo(1);
    }

    @Test
    @DisplayName("오늘·전일 풀이 수는 세션 수가 아니라 문항 수로 센다.")
    void readDashboard_solveCount() {
        LocalDate today = LocalDate.now(KST);
        saveSession(10L, QuestionType.MULTIPLE_CHOICE, 3, today.atTime(9, 0));
        saveSession(20L, QuestionType.ESSAY, 1, today.atTime(10, 0));
        saveSession(30L, QuestionType.MULTIPLE_CHOICE, 3, today.minusDays(1).atTime(9, 0));

        DashboardResult result = adminDashboardService.readDashboard();

        assertThat(result.todaySolveCount().current()).isEqualTo(4);
        assertThat(result.todaySolveCount().previous()).isEqualTo(3);
    }

    @Test
    @DisplayName("누적 풀이 수를 객관식·서술형으로 나눠 합산한다.")
    void readDashboard_cumulativeSolveCount() {
        LocalDate today = LocalDate.now(KST);
        saveSession(10L, QuestionType.MULTIPLE_CHOICE, 3, today.atTime(9, 0));
        saveSession(10L, QuestionType.MULTIPLE_CHOICE, 2, today.minusDays(30).atTime(9, 0));
        saveSession(20L, QuestionType.ESSAY, 1, today.minusDays(40).atTime(9, 0));

        DashboardResult result = adminDashboardService.readDashboard();

        assertThat(result.cumulativeSolveCount().total()).isEqualTo(6);
        assertThat(result.cumulativeSolveCount().multipleChoiceCount()).isEqualTo(5);
        assertThat(result.cumulativeSolveCount().essayCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("최근 7일 활동 회원 수는 같은 회원을 중복해서 세지 않고 전주와 비교한다.")
    void readDashboard_activeMemberCount() {
        LocalDate today = LocalDate.now(KST);
        saveSession(10L, QuestionType.MULTIPLE_CHOICE, 1, today.atTime(9, 0));
        saveSession(10L, QuestionType.MULTIPLE_CHOICE, 1, today.minusDays(6).atTime(9, 0));
        saveSession(20L, QuestionType.MULTIPLE_CHOICE, 1, today.minusDays(3).atTime(9, 0));
        saveSession(30L, QuestionType.MULTIPLE_CHOICE, 1, today.minusDays(7).atTime(9, 0));

        DashboardResult result = adminDashboardService.readDashboard();

        assertThat(result.activeMember7Days().current()).isEqualTo(2);
        assertThat(result.activeMember7Days().previous()).isEqualTo(1);
    }

    @Test
    @DisplayName("오늘 면접 참여 수와 완료 수를 전일과 비교해 조회한다.")
    void readDashboard_interviewMetric() {
        LocalDate today = LocalDate.now(KST);
        dailyInterviewRepository.save(DailyInterviewFixture.inProgress(10L, today));
        dailyInterviewRepository.save(DailyInterviewFixture.completed(20L, today));
        dailyInterviewRepository.save(DailyInterviewFixture.completed(30L, today.minusDays(1)));

        DashboardResult result = adminDashboardService.readDashboard();

        assertThat(result.todayInterview().started().current()).isEqualTo(2);
        assertThat(result.todayInterview().started().previous()).isEqualTo(1);
        assertThat(result.todayInterview().completed().current()).isEqualTo(1);
        assertThat(result.todayInterview().completed().previous()).isEqualTo(1);
    }

    @Test
    @DisplayName("오늘 면접 문항이 고정되지 않으면 미고정 알림을 함께 내려준다.")
    void readDashboard_alertWhenNotPinned() {
        DashboardResult result = adminDashboardService.readDashboard();

        assertThat(result.alerts()).extracting(DashboardAlert::type)
                .containsExactly(DashboardAlertType.DAILY_INTERVIEW_NOT_PINNED);
    }

    @Test
    @DisplayName("오늘 면접 문항이 고정돼 있으면 알림이 없다.")
    void readDashboard_noAlertWhenPinned() {
        dailyInterviewQuestionRepository.save(DailyInterviewQuestion.pin(LocalDate.now(KST), 1L));

        DashboardResult result = adminDashboardService.readDashboard();

        assertThat(result.alerts()).isEmpty();
    }

    @Test
    @DisplayName("데이터가 없는 환경에서는 모든 지표가 0이다.")
    void readDashboard_noData() {
        DashboardResult result = adminDashboardService.readDashboard();

        assertThat(result.totalMemberCount()).isZero();
        assertThat(result.activeMember7Days().current()).isZero();
        assertThat(result.activeMember7Days().previous()).isZero();
        assertThat(result.cumulativeSolveCount().total()).isZero();
        assertThat(result.todaySolveCount().current()).isZero();
        assertThat(result.todaySignUpCount().current()).isZero();
        assertThat(result.todayInterview().started().current()).isZero();
        assertThat(result.todayInterview().completed().current()).isZero();
    }

    private void saveUser(String nickname, LocalDateTime createdAt) {
        userRepository.save(UserFixture.user()
                .email(nickname + "@example.com")
                .nickname(nickname)
                .createdAt(createdAt)
                .build());
    }

    private void saveSession(Long userId, QuestionType type, int totalCount, LocalDateTime solvedAt) {
        solvedSessionRepository.save(SolvedSession.completed(
                userId, type, totalCount, totalCount, solvedAt.minusMinutes(5), solvedAt));
    }
}
