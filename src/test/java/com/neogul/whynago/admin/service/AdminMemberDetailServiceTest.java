package com.neogul.whynago.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.admin.service.dto.AdminMemberDetailResult;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.DailyInterviewFixture;
import com.neogul.whynago.interview.infra.DailyInterviewRepository;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.fixture.UserFixture;
import com.neogul.whynago.user.infra.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminMemberDetailServiceTest extends IntegrationTestSupport {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired
    private AdminMemberDetailService adminMemberDetailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Autowired
    private DailyInterviewRepository dailyInterviewRepository;

    @Test
    @DisplayName("회원의 기본 정보와 스트릭·풀이 문항 수·완료 면접 수를 함께 조회한다.")
    void readMember() {
        // given
        LocalDate today = LocalDate.now(KST);
        User user = saveUser("devhoon", today.atTime(9, 12));
        saveSession(user.getId(), 3, today.atTime(10, 0));
        saveSession(user.getId(), 2, today.minusDays(1).atTime(10, 0));
        dailyInterviewRepository.save(DailyInterviewFixture.completed(user.getId(), today));
        dailyInterviewRepository.save(DailyInterviewFixture.inProgress(user.getId(), today.minusDays(1)));

        // when
        AdminMemberDetailResult result = adminMemberDetailService.readMember(user.getId());

        // then
        assertThat(result.member().nickname()).isEqualTo("devhoon");
        assertThat(result.member().createdAt()).isEqualTo(today.atTime(9, 12));
        assertThat(result.streakDays()).isEqualTo(2);
        assertThat(result.solvedQuestionCount()).isEqualTo(5);
        assertThat(result.completedInterviewCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("다른 회원의 풀이·면접 이력은 집계에 섞이지 않는다.")
    void readMember_excludesOtherMembers() {
        // given
        LocalDate today = LocalDate.now(KST);
        User user = saveUser("devhoon", today.atTime(9, 0));
        User other = saveUser("minakim", today.atTime(9, 0));
        saveSession(user.getId(), 3, today.atTime(10, 0));
        saveSession(other.getId(), 7, today.atTime(10, 0));
        dailyInterviewRepository.save(DailyInterviewFixture.completed(other.getId(), today));

        // when
        AdminMemberDetailResult result = adminMemberDetailService.readMember(user.getId());

        // then
        assertThat(result.solvedQuestionCount()).isEqualTo(3);
        assertThat(result.completedInterviewCount()).isZero();
    }

    @Test
    @DisplayName("이력이 없는 신규 회원의 스트릭·풀이 수·면접 수는 모두 0이다.")
    void readMember_noHistory() {
        // given
        User user = saveUser("newbie", LocalDate.now(KST).atTime(9, 0));

        // when
        AdminMemberDetailResult result = adminMemberDetailService.readMember(user.getId());

        // then
        assertThat(result.streakDays()).isZero();
        assertThat(result.solvedQuestionCount()).isZero();
        assertThat(result.completedInterviewCount()).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 회원을 조회하면 예외가 발생한다.")
    void readMember_notFound() {
        // when & then
        assertThatThrownBy(() -> adminMemberDetailService.readMember(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    private User saveUser(String nickname, LocalDateTime createdAt) {
        return userRepository.save(UserFixture.user()
                .email(nickname + "@example.com")
                .nickname(nickname)
                .createdAt(createdAt)
                .build());
    }

    private void saveSession(Long userId, int totalCount, LocalDateTime solvedAt) {
        solvedSessionRepository.save(SolvedSession.completed(
                userId, QuestionType.MULTIPLE_CHOICE, totalCount, totalCount, solvedAt.minusMinutes(5), solvedAt));
    }
}
