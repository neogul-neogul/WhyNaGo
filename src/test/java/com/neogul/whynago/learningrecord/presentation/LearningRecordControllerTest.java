package com.neogul.whynago.learningrecord.presentation;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.learningrecord.service.dto.DailyRecordCountResult;
import com.neogul.whynago.learningrecord.service.dto.RecentRecordResult;
import com.neogul.whynago.learningrecord.service.dto.StreakResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class LearningRecordControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("최근 기록 목록을 조회한다.")
    void findRecent() {
        given(learningRecordService.findRecent(10L, 20)).willReturn(List.of(new RecentRecordResult(
                1L, QuestionType.MULTIPLE_CHOICE, Category.NETWORK, 3, 2, 1,
                LocalDateTime.of(2026, 6, 25, 9, 58), LocalDateTime.of(2026, 6, 25, 10, 16)
        )));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/learning-records/recent")
                .then()
                .statusCode(200)
                .body("[0].sessionId", Matchers.equalTo(1))
                .body("[0].category", Matchers.equalTo("NETWORK"))
                .body("[0].wrongCount", Matchers.equalTo(1));
    }

    @Test
    @DisplayName("size 쿼리 파라미터로 최근 기록 개수를 지정한다.")
    void findRecent_withSize() {
        given(learningRecordService.findRecent(eq(10L), eq(5))).willReturn(List.of());

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .queryParam("size", 5)
                .when()
                .get("/api/learning-records/recent")
                .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(0));
    }

    @Test
    @DisplayName("연속·누적 학습일을 조회한다.")
    void getStreak() {
        given(learningRecordService.getStreak(10L)).willReturn(new StreakResult(7, 42));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/learning-records/streak")
                .then()
                .statusCode(200)
                .body("streakDays", Matchers.equalTo(7))
                .body("cumulativeDays", Matchers.equalTo(42));
    }

    @Test
    @DisplayName("기간을 지정해 일자별 학습량을 조회한다.")
    void findDailyCounts() {
        given(learningRecordService.findDailyCounts(10L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 25)))
                .willReturn(List.of(new DailyRecordCountResult(LocalDate.of(2026, 6, 25), 2, 5)));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .queryParam("from", "2026-06-01")
                .queryParam("to", "2026-06-25")
                .when()
                .get("/api/learning-records/daily-counts")
                .then()
                .statusCode(200)
                .body("[0].date", Matchers.equalTo("2026-06-25"))
                .body("[0].sessionCount", Matchers.equalTo(2))
                .body("[0].questionCount", Matchers.equalTo(5));
    }
}
