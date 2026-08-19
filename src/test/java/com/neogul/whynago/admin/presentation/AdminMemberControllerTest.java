package com.neogul.whynago.admin.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.neogul.whynago.admin.service.dto.AdminMemberDetailResult;
import com.neogul.whynago.admin.service.dto.AdminMemberResult;
import com.neogul.whynago.admin.service.dto.AdminMemberSummaryResult;
import com.neogul.whynago.admin.service.dto.AdminMembersResult;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.domain.Role;
import com.neogul.whynago.user.exception.UserErrorCode;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class AdminMemberControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("관리자가 회원 목록을 조회하면 200과 페이징된 회원 정보를 반환한다.")
    void findMembers() {
        given(adminMemberListService.readMembers(any())).willReturn(new AdminMembersResult(
                List.of(member()),
                0,
                8,
                1204L
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .queryParam("page", 0)
                .queryParam("size", 8)
                .when()
                .get("/api/admin/members")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(1))
                .body("content[0].id", Matchers.equalTo(20481))
                .body("content[0].nickname", Matchers.equalTo("devhoon"))
                .body("content[0].email", Matchers.equalTo("devhoon@gmail.com"))
                .body("content[0].position", Matchers.equalTo("BACKEND"))
                .body("content[0].provider", Matchers.equalTo("GOOGLE"))
                .body("content[0].createdAt", Matchers.equalTo("2025-11-02T09:12:00"))
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(8))
                .body("totalElements", Matchers.equalTo(1204));
    }

    @Test
    @DisplayName("가입 시각이 없는 회원의 가입일은 null로 응답한다.")
    void findMembers_createdAtIsNull() {
        given(adminMemberListService.readMembers(any())).willReturn(new AdminMembersResult(
                List.of(new AdminMemberResult(
                        1L, "legacy", "legacy@example.com", Position.BACKEND, AuthProvider.LOCAL, null)),
                0,
                8,
                1L
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/members")
                .then()
                .statusCode(200)
                .body("content[0].createdAt", Matchers.nullValue());
    }

    @Test
    @DisplayName("관리자가 회원 요약을 조회하면 200과 전체·최근 7일 활동 회원 수를 반환한다.")
    void findSummary() {
        given(adminMemberListService.readSummary()).willReturn(new AdminMemberSummaryResult(1204L, 312L));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/members/summary")
                .then()
                .statusCode(200)
                .body("totalCount", Matchers.equalTo(1204))
                .body("activeWeekCount", Matchers.equalTo(312));
    }

    @Test
    @DisplayName("관리자가 회원 상세를 조회하면 200과 지표를 함께 반환한다.")
    void findMember() {
        given(adminMemberDetailService.readMember(20481L))
                .willReturn(new AdminMemberDetailResult(member(), 62, 1208L, 214L));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/members/20481")
                .then()
                .statusCode(200)
                .body("nickname", Matchers.equalTo("devhoon"))
                .body("streakDays", Matchers.equalTo(62))
                .body("solvedQuestionCount", Matchers.equalTo(1208))
                .body("completedInterviewCount", Matchers.equalTo(214));
    }

    @Test
    @DisplayName("존재하지 않는 회원을 조회하면 404를 응답한다.")
    void findMember_notFound() {
        willThrow(new BusinessException(UserErrorCode.USER_NOT_FOUND))
                .given(adminMemberDetailService).readMember(999L);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/members/999")
                .then()
                .statusCode(404)
                .body("code", Matchers.equalTo("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("일반 사용자가 회원 목록을 조회하면 403을 응답한다.")
    void findMembers_notAdmin() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/admin/members")
                .then()
                .statusCode(403)
                .body("code", Matchers.equalTo("AUTH_FORBIDDEN"));
    }

    private AdminMemberResult member() {
        return new AdminMemberResult(
                20481L,
                "devhoon",
                "devhoon@gmail.com",
                Position.BACKEND,
                AuthProvider.GOOGLE,
                LocalDateTime.of(2025, 11, 2, 9, 12)
        );
    }
}
