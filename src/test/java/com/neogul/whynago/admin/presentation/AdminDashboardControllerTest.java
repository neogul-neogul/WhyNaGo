package com.neogul.whynago.admin.presentation;

import static org.mockito.BDDMockito.given;

import com.neogul.whynago.admin.implement.dto.DashboardAlert;
import com.neogul.whynago.admin.service.dto.CumulativeSolveCount;
import com.neogul.whynago.admin.service.dto.DashboardResult;
import com.neogul.whynago.admin.service.dto.InterviewMetric;
import com.neogul.whynago.admin.service.dto.MetricComparison;
import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.user.domain.Role;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class AdminDashboardControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("관리자가 대시보드를 조회하면 200과 지표·알림을 반환한다.")
    void findDashboard() {
        given(adminDashboardService.readDashboard()).willReturn(new DashboardResult(
                1204L,
                new MetricComparison(312L, 294L),
                new CumulativeSolveCount(48920L, 34180L, 14740L),
                new MetricComparison(1284L, 1142L),
                new MetricComparison(37L, 29L),
                new InterviewMetric(new MetricComparison(412L, 435L), new MetricComparison(323L, 340L)),
                List.of(DashboardAlert.dailyInterviewNotPinned(LocalDate.of(2026, 8, 19)))
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/dashboard")
                .then()
                .statusCode(200)
                .body("totalMemberCount", Matchers.equalTo(1204))
                .body("activeMember7Days.current", Matchers.equalTo(312))
                .body("activeMember7Days.previous", Matchers.equalTo(294))
                .body("cumulativeSolveCount.total", Matchers.equalTo(48920))
                .body("cumulativeSolveCount.multipleChoiceCount", Matchers.equalTo(34180))
                .body("cumulativeSolveCount.essayCount", Matchers.equalTo(14740))
                .body("todaySolveCount.current", Matchers.equalTo(1284))
                .body("todaySignUpCount.current", Matchers.equalTo(37))
                .body("todayInterview.started.current", Matchers.equalTo(412))
                .body("todayInterview.completed.previous", Matchers.equalTo(340))
                .body("alerts", Matchers.hasSize(1))
                .body("alerts[0].type", Matchers.equalTo("DAILY_INTERVIEW_NOT_PINNED"))
                .body("alerts[0].interviewDate", Matchers.equalTo("2026-08-19"));
    }

    @Test
    @DisplayName("알림이 없으면 빈 배열을 반환한다.")
    void findDashboard_noAlerts() {
        given(adminDashboardService.readDashboard()).willReturn(new DashboardResult(
                0L,
                new MetricComparison(0L, 0L),
                new CumulativeSolveCount(0L, 0L, 0L),
                new MetricComparison(0L, 0L),
                new MetricComparison(0L, 0L),
                new InterviewMetric(new MetricComparison(0L, 0L), new MetricComparison(0L, 0L)),
                List.of()
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/dashboard")
                .then()
                .statusCode(200)
                .body("alerts", Matchers.hasSize(0));
    }

    @Test
    @DisplayName("일반 사용자가 대시보드를 조회하면 403을 응답한다.")
    void findDashboard_notAdmin() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/admin/dashboard")
                .then()
                .statusCode(403)
                .body("code", Matchers.equalTo("AUTH_FORBIDDEN"));
    }
}
