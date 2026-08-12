package com.neogul.whynago.progress.presentation;

import static org.mockito.BDDMockito.given;

import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.service.dto.ProgressDetailResult;
import com.neogul.whynago.progress.service.dto.ProgressSummaryResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class ProgressControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("점수·티어·카테고리별 풀이 문제 수를 조회한다.")
    void getProgress() {
        given(progressService.getDetail(10L)).willReturn(new ProgressDetailResult(
                42, Tier.SILVER, Tier.GOLD, 723, 15, Map.of(Category.NETWORK, 5, Category.DB, 10)
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/progress")
                .then()
                .statusCode(200)
                .body("score", Matchers.equalTo(42))
                .body("tier", Matchers.equalTo("SILVER"))
                .body("nextTier", Matchers.equalTo("GOLD"))
                .body("scoreToNextTier", Matchers.equalTo(723))
                .body("totalQuestionCount", Matchers.equalTo(15))
                .body("categoryQuestionCounts.NETWORK", Matchers.equalTo(5))
                .body("categoryQuestionCounts.DB", Matchers.equalTo(10));
    }

    @Test
    @DisplayName("최고 티어이면 다음 티어 없이(null) 응답한다.")
    void getProgress_maxTier() {
        given(progressService.getDetail(10L)).willReturn(new ProgressDetailResult(
                3000, Tier.DIAMOND, null, 0, 500, Map.of()
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/progress")
                .then()
                .statusCode(200)
                .body("tier", Matchers.equalTo("DIAMOND"))
                .body("nextTier", Matchers.nullValue())
                .body("scoreToNextTier", Matchers.equalTo(0));
    }

    @Test
    @DisplayName("누적/연속 학습일·총 풀이 문제/정답/오답·1일1면접 횟수를 조회한다.")
    void getSummary() {
        given(progressService.getSummary(10L)).willReturn(new ProgressSummaryResult(42, 7, 128, 96, 32, 16));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/progress/summary")
                .then()
                .statusCode(200)
                .body("cumulativeDays", Matchers.equalTo(42))
                .body("streakDays", Matchers.equalTo(7))
                .body("totalQuestionCount", Matchers.equalTo(128))
                .body("totalCorrectCount", Matchers.equalTo(96))
                .body("totalWrongCount", Matchers.equalTo(32))
                .body("completedInterviewCount", Matchers.equalTo(16));
    }
}
