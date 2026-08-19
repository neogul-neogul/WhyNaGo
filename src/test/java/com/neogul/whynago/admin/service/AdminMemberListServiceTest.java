package com.neogul.whynago.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.admin.service.dto.AdminMemberResult;
import com.neogul.whynago.admin.service.dto.AdminMemberSearchCommand;
import com.neogul.whynago.admin.service.dto.AdminMemberSummaryResult;
import com.neogul.whynago.admin.service.dto.AdminMembersResult;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.fixture.UserFixture;
import com.neogul.whynago.user.infra.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminMemberListServiceTest extends IntegrationTestSupport {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Autowired
    private AdminMemberListService adminMemberListService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Test
    @DisplayName("회원 목록의 기본 정보를 조회한다.")
    void readMembers() {
        // given
        User user = saveUser("devhoon", LocalDateTime.of(2026, 8, 19, 9, 12));

        // when
        AdminMembersResult result = adminMemberListService.readMembers(searchCommand(null, 0, 8));

        // then
        assertThat(result.members()).hasSize(1);
        AdminMemberResult member = result.members().getFirst();
        assertThat(member.id()).isEqualTo(user.getId());
        assertThat(member.nickname()).isEqualTo("devhoon");
        assertThat(member.email()).isEqualTo("devhoon@example.com");
        assertThat(member.position()).isEqualTo(Position.BACKEND);
        assertThat(member.provider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(member.createdAt()).isEqualTo(LocalDateTime.of(2026, 8, 19, 9, 12));
    }

    @Test
    @DisplayName("가입 시각이 없는 회원의 가입일은 null로 내려간다.")
    void readMembers_createdAtIsNull() {
        // given
        userRepository.save(UserFixture.user()
                .email("legacy@example.com").nickname("legacy")
                .createdAtNotTracked()
                .build());

        // when
        AdminMembersResult result = adminMemberListService.readMembers(searchCommand(null, 0, 8));

        // then
        assertThat(result.members().getFirst().createdAt()).isNull();
    }

    @Test
    @DisplayName("닉네임·이메일 검색어로 회원을 걸러낸다.")
    void readMembers_keyword() {
        // given
        saveUser("devhoon", LocalDateTime.of(2026, 8, 19, 9, 0));
        saveUser("minakim", LocalDateTime.of(2026, 8, 19, 10, 0));

        // when
        AdminMembersResult result = adminMemberListService.readMembers(searchCommand("mina", 0, 8));

        // then
        assertThat(result.members()).extracting(AdminMemberResult::nickname).containsExactly("minakim");
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("가입 역순으로 요청한 페이지만 조회하고 전체 회원 수를 함께 내려준다.")
    void readMembers_paged() {
        // given
        saveUser("firstus", LocalDateTime.of(2026, 8, 17, 9, 0));
        User second = saveUser("second", LocalDateTime.of(2026, 8, 18, 9, 0));
        saveUser("thirdus", LocalDateTime.of(2026, 8, 19, 9, 0));

        // when
        AdminMembersResult result = adminMemberListService.readMembers(searchCommand(null, 1, 1));

        // then
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.members()).extracting(AdminMemberResult::id).containsExactly(second.getId());
    }

    @Test
    @DisplayName("요약은 전체 회원 수와 최근 7일 활동 회원 수를 조회한다.")
    void readSummary() {
        // given
        LocalDate today = LocalDate.now(KST);
        User active = saveUser("active", today.atTime(9, 0));
        User idle = saveUser("idleone", today.atTime(9, 0));
        saveSession(active.getId(), today.atTime(10, 0));
        saveSession(idle.getId(), today.minusDays(10).atTime(10, 0));

        // when
        AdminMemberSummaryResult result = adminMemberListService.readSummary();

        // then
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.activeWeekCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("회원이 없으면 목록은 비고 요약은 0이다.")
    void readMembers_noMember() {
        // when
        AdminMembersResult members = adminMemberListService.readMembers(searchCommand(null, 0, 8));
        AdminMemberSummaryResult summary = adminMemberListService.readSummary();

        // then
        assertThat(members.members()).isEmpty();
        assertThat(members.totalElements()).isZero();
        assertThat(summary.totalCount()).isZero();
        assertThat(summary.activeWeekCount()).isZero();
    }

    private AdminMemberSearchCommand searchCommand(String keyword, int page, int size) {
        return AdminMemberSearchCommand.of(keyword, page, size);
    }

    private User saveUser(String nickname, LocalDateTime createdAt) {
        return userRepository.save(UserFixture.user()
                .email(nickname + "@example.com")
                .nickname(nickname)
                .createdAt(createdAt)
                .build());
    }

    private void saveSession(Long userId, LocalDateTime solvedAt) {
        solvedSessionRepository.save(SolvedSession.completed(
                userId, QuestionType.MULTIPLE_CHOICE, 3, 3, solvedAt.minusMinutes(5), solvedAt));
    }
}
